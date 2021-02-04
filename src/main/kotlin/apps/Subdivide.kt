package apps

import aBeLibs.color.ColorProviderImage
import aBeLibs.extensions.NoJitter
import aBeLibs.fx.WideColorCorrection
import aBeLibs.geometry.longest
import aBeLibs.math.angleDiff
import aBeLibs.shadestyles.Addjust
import aBeLibs.shadestyles.PerpendicularGradient
import apps.live.treeShadowTexture
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.openrndr.*
import org.openrndr.color.ColorRGBa
import org.openrndr.dialogs.openFileDialog
import org.openrndr.dialogs.saveFileDialog
import org.openrndr.draw.*
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.fx.blend.Add
import org.openrndr.extra.fx.blur.ApproximateGaussianBlur
import org.openrndr.extra.gui.GUI
import org.openrndr.extra.noise.Random
import org.openrndr.extra.parameters.ActionParameter
import org.openrndr.extra.parameters.Description
import org.openrndr.extra.parameters.DoubleParameter
import org.openrndr.extra.parameters.OptionParameter
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import org.openrndr.shape.Segment
import org.openrndr.shape.ShapeContour
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.roundToLong
import kotlin.system.exitProcess

@Suppress("KDocUnresolvedReference")
/**
 * Basic template
 * - [x] Sometimes gradient rotated 180°. Fixed by implementing distance based pependicular gradient.
 * - [x] Allow non-random cuts. Drag to set direction.
 * - [x] Keep color when splitting. By using noise of contour bounds center.
 * - [x] Allow drag to set cut direction in requested direction and rotated 180°
 * - [x] Implement multilevel undo. Currently it removes one curve and adds two. Instead of removing it,
 *      move it to the undo stack.
 * - [x] Fix no intersection but collision
 * - [x] Add int slider for number of apps.getAngles 2, 3, 5, 7
 * - [x] Add Clear button
 * - [x] Broke opposite apps.getAngles. Fix it.
 * - [x] Allow setting color manually for each shape? Currently curves is a list of ShapeContour, but that can't
store extra info. Make a new data class? With ShapeContour + ColorRGBa?
 * - [x] Add slider for rotation offset (currently it's 0°)
 * - [x] Textures. Image based? Shader based?
 * - [x] Adjustable brightness
 * - [x] Adjustable contrast
 * - [x] Adjustable bloom cutoff
 * - [x] Add radial gradient for metallic/highlight effect. Maybe add pos2 to PerpendicularGradient.kt?
 * - [x] Optimization: don't redraw the whole thing on every frame, only when it changes.
 * - [x] Initial state odd. Nothing shown until I interact. Even if I set dirty = true at start.
 * - [x] Implement save and load json
 * - [x] Implement "tree leaf shadows" by creating an additive texture concentrated around branches, blurred.
 * - [ ] Spray delete. Delete also any lines leading to the current line. Do I need a data structure to keep
 *       track of which lines lines end at?
 *       What about undo delete?
 * - [ ] Sometimes there's unexpected behavior. Colors can't be changed, borders look pixelated. Is it
 *       from overlapping lines? Print debug info on every action to figure out.
 * - [ ] Record all interactions to be able to animate the creation?
 * - [ ] Snap mouse pos at start, choose target at end. Show snapping point.
 * - [ ] Change data structure? It's actually a tree. Draw only items with no children. If you delete a shape,
 *       also delete all siblings.
 *       How would undo/redo work then? Apparently the way to have undo/redo is to have a stack of commands.
 *       Commands are Split, SetColor, Delete
 */

enum class Cuts { THREE, FOUR, FIVE, SEVEN }

val angles = mapOf(Cuts.THREE to 360.0 / 3, Cuts.FOUR to 360.0 / 4, Cuts.FIVE to 360.0 / 5, Cuts.SEVEN to 360.0 / 7)

/**
 * A ShapeContour with a colorOffset
 */
data class Piece(@Transient var shape: ShapeContour, var colorOffset: Double) {
    // Serialize this
    private var points: List<Vector2> = shape.segments.map { it.start }

    // After deserialization call rebuild()
    fun rebuild() {
        shape = ShapeContour.fromPoints(points, true)
    }
}

/**
 * A serializable Design, including texPathA, colorsPath, pieces, ColorProviderImage, guiState
 * and two transient: texA and texB
 */
data class Design(
    var texPathA: String,
    @Transient var lightTexture: ColorBuffer,
    var colorsPath: String
) {
    @Transient
    var texA: ColorBuffer = loadImage(texPathA)
    var pieces = mutableListOf<Piece>()
    val colors = ColorProviderImage(colorsPath)
    lateinit var guiState: Map<String, Map<String, GUI.ParameterValue>>

    fun rebuild(lightTex: ColorBuffer) {
        lightTexture = lightTex
        texA = loadImage(texPathA)
        colors.imgPath = colors.imgPath
        pieces.forEach { piece -> piece.rebuild() }
        println("rebuild() -> ${colors.imgPath}")
    }
}

@ExperimentalStdlibApi
fun main() = application {
    configure {
        width = 1920
        height = 1080
    }

    program {
        var totalDrag = Vector2.ZERO
        var validDrag = false
        var showColorOffset = 0.0
        val gui = GUI()
        var targetPiece: Piece? = null
        val undo = mutableListOf<Piece>()
        var dirty = false
        val bufGradients = renderTarget(width, height) {
            colorBuffer(ColorFormat.RGBa, ColorType.FLOAT32)
            depthBuffer()
        }
        val bufColorCorrected = bufGradients.colorBuffer(0).createEquivalent()
        val bufBlurred = bufColorCorrected.createEquivalent()
        val bufGradientsBlurred = bufColorCorrected.createEquivalent()
        val bufFinal = bufColorCorrected.createEquivalent()
        val colorCorrection = WideColorCorrection().apply {
            brightness = -0.5
            contrast = 1.0
            saturation = -0.3
            gamma = 1.5
            opacity = 1.0
        }
        val blur = ApproximateGaussianBlur().apply {
            sigma = 25.0
            window = 25
            gain = 1.1
        }
        val gradient = PerpendicularGradient(
            ColorRGBa.BLACK, ColorRGBa.WHITE,
            offset = Vector2.ZERO,
            exponent = 0.7
        )
        val add = Add()
        val addjust = Addjust()
        val screenshot = Screenshots()
        var design = Design(
            "data/images/scratched-and-scraped-metal-texture-12.jpg",
            treeShadowTexture(drawer, width, height),
            //"data/images/leaves.jpg",
            "data/textures/"
        )

        fun clear() {
            design.pieces.clear()
            undo.clear()
            design.pieces.add(
                Piece(
                    Rectangle(0.0, 0.0, width * 1.0, height * 1.0).contour,
                    Random.double0(360.0)
                )
            )
        }
        clear()
        gradient.texA = design.texA

        val colorParams = @Description("Color") object {
            @DoubleParameter("shift", 0.0, 360.0, precision = 0)
            var colorShift = 0.0

            @DoubleParameter("radius", 0.0, 600.0, precision = 0)
            var colorRadius = 100.0

            @ActionParameter("randomize") @Suppress("unused")
            fun colorChange() {
                design.colors.reset()
                dirty = true
            }

            @ActionParameter("Load") @Suppress("unused")
            fun load() {
                openFileDialog(supportedExtensions = listOf("JPG", "jpg")) {
                    design.colors.imgPath = it.absolutePath
                }
            }
        }

        val generalParams = @Description("General") object {
            @DoubleParameter("angle offset", 0.0, 360.0, precision = 0)
            var angleOffset = 0.0

            @OptionParameter("Subdivisions")
            var cuts = Cuts.THREE

            @ActionParameter("Screenshot") @Suppress("unused")
            fun takeScreenshot() {
                screenshot.trigger()
            }

            @ActionParameter("Save") @Suppress("unused")
            fun doSave() {
                saveFileDialog(supportedExtensions = listOf("json")) {
                    design.guiState = gui.toObject()
                    val gson = GsonBuilder().setPrettyPrinting().create()
                    val json = gson.toJson(design)
                    it.writeText(json)
                }
            }

            @ActionParameter("Load") @Suppress("unused")
            fun doLoad() {
                openFileDialog(supportedExtensions = listOf("json")) {
                    val gson = Gson()
                    val typeToken = object : TypeToken<Design>() {}
                    design = gson.fromJson(it.readText(), typeToken.type)
                    gui.fromObject(design.guiState)
                    design.rebuild(treeShadowTexture(drawer, width, height))
                    dirty = true
                }
            }

            @ActionParameter("Clear") @Suppress("unused")
            fun clear() {
                clear()
            }
        }

        fun angleInc(): Double {
            return angles.getOrDefault(generalParams.cuts, 180.0)
        }

        fun doSplit(pos: Vector2, angle: Double = Random.double0(360 / angleInc()).toInt() * angleInc()) {
            val off = Polar(angle, 3000.0).cartesian
            val victim = design.pieces.firstOrNull { it.shape.contains(pos) }
            if (victim != null) {
                val knife = Segment(pos + off, pos - off).contour
                val parts = victim.shape.split(knife)
                println("${parts[0].length}, ${parts[1].length}")
                if (parts[0].segments.isNotEmpty()) {
                    undo.add(victim)
                    design.pieces.remove(victim)
                    design.pieces.add(Piece(parts[0].close, victim.colorOffset))
                    design.pieces.add(Piece(parts[1].close,
                        (victim.colorOffset + 1) % 360))
                }
                dirty = true
            }
        }

        @ExperimentalStdlibApi
        fun undo() {
            if (undo.size > 0) {
                design.pieces.removeLast()
                design.pieces.removeLast()
                design.pieces.add(undo.removeLast())
                dirty = true
            }
        }

        extend(screenshot)
        extend(NoJitter())
        extend(gui) {
            compartmentsCollapsedByDefault = false
            add(colorCorrection)
            add(blur)
            add(gradient)
            add(addjust)
            add(generalParams)
            add(colorParams)
        }
        gui.onChange { _, _ -> dirty = true }

        extend {
            design.colors.radius = colorParams.colorRadius

            if (dirty || frameCount == 5) {
                drawer.isolatedWithTarget(bufGradients) {
                    stroke = null
                    design.pieces.forEach { piece ->
                        val rgb = design.colors.getColor(piece.colorOffset + colorParams.colorShift)
                        val longest = piece.shape.longest()
                        val dir = longest.direction()
                        gradient.apply {
                            color0 = rgb.shade(1.2)
                            color1 = rgb.shade(0.6)
                            offset = longest.start
                            rotation = Math.toDegrees(atan2(dir.y, dir.x)) + 90
                        }
                        drawer.shadeStyle = gradient
                        drawer.contour(piece.shape)
                    }
                }
                colorCorrection.apply(bufGradients.colorBuffer(0), bufColorCorrected)
                blur.apply(bufColorCorrected, bufBlurred)
                add.apply(arrayOf(bufGradients.colorBuffer(0), bufBlurred), bufGradientsBlurred)
                addjust.apply(arrayOf(bufGradientsBlurred, design.lightTexture), bufFinal)

                dirty = false
            }
            drawer.image(bufFinal)

            targetPiece?.let {
                drawer.fill = null
                drawer.stroke = ColorRGBa.WHITE
                drawer.contour(it.shape)
            }

            //Random.resetState()
            if (keyboard.pressedKeys.contains("right-control")) {
                val img = design.colors.getImage()
                img?.let {
                    drawer.image(it, drawer.bounds.center - it.bounds.center)
                }
                drawer.fill = null
                drawer.stroke = ColorRGBa.WHITE
                drawer.circle(drawer.bounds.center, design.colors.radius)
                drawer.circle(drawer.bounds.center + Polar(showColorOffset, design.colors.radius).cartesian, 10.0)
            }
            if (keyboard.pressedKeys.contains("left-alt")) {
                val w = width * 0.2
                val h = height * 0.2
                drawer.image(bufGradients.colorBuffer(0), w * 2, h * 3, w, h)
                drawer.image(bufColorCorrected, w * 3, h * 3, w, h)
                drawer.image(bufBlurred, w * 4, h * 3, w, h)
            }
        }
        //extend(FPSDisplay(font))

        // -------- Interaction -------------
        mouse.buttonDown.listen { event ->
            validDrag = !event.propagationCancelled
            totalDrag = Vector2.ZERO
            targetPiece = design.pieces.firstOrNull { it.shape.contains(event.position) }
        }
        mouse.dragged.listen { event ->
            totalDrag += event.dragDisplacement
            if (validDrag && event.button == MouseButton.RIGHT) {
                targetPiece?.let {
                    it.colorOffset += event.dragDisplacement.x
                    showColorOffset = it.colorOffset
                    dirty = true
                }
            }
        }
        mouse.buttonUp.listen { event ->
            // avoid UI panel
            if (validDrag && event.button == MouseButton.LEFT && !keyboard.pressedKeys.contains("left-shift")) {
                val requested = Polar.fromVector(totalDrag).theta
                val opposite = requested + 180

                val quantized0 =
                    ((requested - generalParams.angleOffset) / angleInc()).roundToLong() * angleInc() + generalParams.angleOffset
                val diff0 = abs(angleDiff(quantized0, requested))

                val quantized1 =
                    ((opposite - generalParams.angleOffset) / angleInc()).roundToLong() * angleInc() + generalParams.angleOffset
                val diff1 = abs(angleDiff(quantized1, opposite))

                val cutAngle = if (diff0 < diff1) quantized0 else quantized1
                doSplit(event.position, cutAngle)
            }
        }
        keyboard.keyDown.listen {
            when (it.key) {
                KEY_ESCAPE -> exitProcess(0)
                KEY_ENTER -> design.colors.reset()
                else -> {
                    when (it.name) {
                        "z" -> if (it.modifiers.contains(KeyModifier.CTRL)) undo()
                    }
                }
            }
        }
        window.drop.listen {
            println("${it.files.size} files dropped at ${it.position}")
            println(it.files)
        }
    }
}
