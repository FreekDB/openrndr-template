import color.ColorProviderImage
import extensions.FPSDisplay
import geometry.contains
import geometry.longest
import geometry.split
import math.angleDiff
import org.openrndr.KEY_ENTER
import org.openrndr.KEY_ESCAPE
import org.openrndr.KeyModifier
import org.openrndr.application
import org.openrndr.draw.*
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.fx.blur.GaussianBloom
import org.openrndr.extra.gui.GUI
import org.openrndr.extra.noise.Random
import org.openrndr.extra.parameters.*
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import org.openrndr.shape.Segment
import org.openrndr.shape.ShapeContour
import shadestyles.perpendicularGradient
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.roundToLong
import kotlin.system.exitProcess

/**
 * Basic template
 * - [x] Sometimes gradient rotated 180°. Fixed by implementing distance based pependicular gradient.
 * - [x] Allow non-random cuts. Drag to set direction.
 * - [x] Keep color when splitting. By using noise of contour bounds center.
 * - [x] Allow drag to set cut direction in requested direction and rotated 180°
 * - [x] Implement multilevel undo. Currently it removes one curve and adds two. Instead of removing it,
 *      move it to the undo stack.
 * - [x] Fix no intersection but collision
 * - [x] Add int slider for number of angles 2, 3, 5, 7
 * - [x] Add Clear button
 * - [x] Broke opposite angles. Fix it.
 * - [] Apply colors via shader? Why
 * - [] Allow setting color manually for each shape?
 * - [] Add slider for rotation offset (currently it's 0°)
 * - [] Textures. Image based? Shader based?
 * - [] Adjustable brightness
 * - [] Adjustable contrast
 */

enum class Cuts { THREE, FOUR, FIVE, SEVEN }

val angles = mapOf(Cuts.THREE to 360.0 / 3, Cuts.FOUR to 360.0 / 4, Cuts.FIVE to 360.0 / 5, Cuts.SEVEN to 360.0 / 7)

@ExperimentalStdlibApi
fun main() = application {
    configure {
        width = 1500
        height = 800
    }

    program {
        var totalDrag = Vector2.ZERO
        var validDrag = false
        val font = loadFont("data/fonts/IBMPlexMono-Regular.ttf", 24.0)
        val curves = mutableListOf<ShapeContour>()
        val undo = mutableListOf<ShapeContour>()
        val colors = ColorProviderImage()
        val src = renderTarget(width, height) {
            colorBuffer(ColorFormat.RGBa, ColorType.FLOAT32)
            depthBuffer()
        }
        val target = colorBuffer(width, height, 1.0, ColorFormat.RGBa, ColorType.FLOAT32)
        val bloom = GaussianBloom()
        bloom.sigma = 0.6
        bloom.window = 5
        bloom.gain = 0.6

        fun clear() {
            curves.clear()
            undo.clear()
            curves.add(Rectangle(0.0, 0.0, width * 1.0, height * 1.0).contour)
        }
        clear()

        val params = @Description("parameters") object {
            @BooleanParameter("bloom")
            var bloomEnabled = false
            @DoubleParameter("color shift", 0.0, 360.0)
            var colorShift = 0.0
            @DoubleParameter("angle offset", 0.0, 360.0)
            var angleOffset = 0.0
            @OptionParameter("Subdivisions")
            var cuts = Cuts.THREE

            @ActionParameter("Change colors")
            fun changeColors() {
                colors.reset()
            }

            @ActionParameter("Clear")
            fun clear() {
                clear()
            }
        }

        fun angleInc(): Double {
            return angles.getOrDefault(params.cuts, 180.0)
        }

        fun doSplit(pos: Vector2, angle: Double = Random.double0(360 / angleInc()).toInt() * angleInc()) {
            val off = Polar(angle, 3000.0).cartesian
            val curve = curves.firstOrNull { it.contains(pos) }
            if (curve != null) {
                val knife = Segment(pos + off, pos - off)
                val parts = curve.split(knife)
                if (parts.first.segments.isNotEmpty()) {
                    undo.add(curve)
                    curves.remove(curve)
                    curves.add(parts.first)
                    curves.add(parts.second)
                }
            }
        }

        @ExperimentalStdlibApi
        fun undo() {
            if (undo.size > 0) {
                curves.removeLast()
                curves.removeLast()
                curves.add(undo.removeLast())
            }
        }

        extend(GUI()) {
            compartmentsCollapsedByDefault = false
            add(bloom)
            add(params)
        }
        extend(Screenshots())
        extend {
            drawer.isolatedWithTarget(src) {
                with(drawer) {
                    stroke = null
                    fontMap = font
                }

                Random.resetState()
                curves.forEach {
                    val pos = it.bounds.center
                    val rgb = colors.getColor(360 * Random.simplex(pos.x, pos.y) + params.colorShift)
                    val longest = it.longest()
                    val dir = longest.direction()
                    val orientation = Math.toDegrees(atan2(dir.y, dir.x))
                    drawer.shadeStyle = perpendicularGradient(
                        rgb.shade(1.2), rgb.shade(0.3),
                        rotation = orientation + 90.0,
                        offset = longest.start,
                        exponent = 0.1
                    )
                    drawer.contour(it)
                }
            }
            if (params.bloomEnabled) {
                bloom.apply(src.colorBuffer(0), target)
                drawer.image(target)
            } else {
                drawer.image(src.colorBuffer(0))
            }
        }
        extend(FPSDisplay(font))

        // -------- Interaction -------------
        mouse.buttonDown.listen {
            validDrag = !it.propagationCancelled
            totalDrag = Vector2.ZERO
        }
        mouse.dragged.listen {
            totalDrag += it.dragDisplacement
        }
        mouse.buttonUp.listen {
            // avoid UI panel
            if (validDrag) {
                val requested = Polar.fromVector(totalDrag).theta
                val opposite = requested + 180
                val quantized = (requested / angleInc()).roundToLong() * angleInc()
                val quantizedOpposite = (opposite / angleInc()).roundToLong() * angleInc()
                val diff = abs(angleDiff(quantized, requested))
                val diffOpposite = abs(angleDiff(quantizedOpposite, opposite))
                doSplit(it.position, if (diff < diffOpposite) quantized else quantizedOpposite)
            }
        }
        keyboard.keyDown.listen {
            when (it.key) {
                KEY_ESCAPE -> exitProcess(0)
                KEY_ENTER -> colors.reset()
                else -> {
                    when (it.name) {
                        "z" -> if (it.modifiers.contains(KeyModifier.CTRL)) undo()
                    }
                }
            }
        }
    }
}
