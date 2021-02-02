package apps

import aBeLibs.extensions.NoJitter
import aBeLibs.geometry.Human
import aBeLibs.geometry.toContours
import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.dialogs.saveFileDialog
import org.openrndr.draw.colorBuffer
import org.openrndr.draw.isolated
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.renderTarget
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.fx.blur.ApproximateGaussianBlur
import org.openrndr.extra.fx.distort.Perturb
import org.openrndr.extra.gui.GUI
import org.openrndr.extra.noise.Random
import org.openrndr.extra.noise.uniform
import org.openrndr.extra.parameters.ActionParameter
import org.openrndr.extra.parameters.Description
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.math.map
import org.openrndr.shape.Circle
import org.openrndr.shape.CompositionDrawer
import org.openrndr.shape.Rectangle
import org.openrndr.shape.ShapeContour
import org.openrndr.svg.writeSVG
import java.io.File
import kotlin.math.pow
import kotlin.system.exitProcess


/**
 * Simple BoofCV test. Loads an image, makes it black and white, gets contours
 */

fun main() = application {
    configure {
        width = 1920
        height = 1080
    }
    program {
        val gui = GUI()
        val bw = renderTarget(width, height) {
            colorBuffer()
        }
        val bwBlurred = colorBuffer(width, height)
        val withFX = colorBuffer(width, height)
        val blur = ApproximateGaussianBlur()
        val fx = Perturb()
        val contours = mutableListOf<ShapeContour>()
        val screenshots = Screenshots()
        var showBW = false

        Random.seed = System.currentTimeMillis().toString()

        fun newImage() {
            drawer.isolatedWithTarget(bw) {
                clear(ColorRGBa.BLACK)
                translate(bounds.center)

                val human = Human(width, height)

                val amount = 100
                val shapes = List(amount) {
                    val pc = map(0.0, amount * 1.0, 1.0, 0.0, it * 1.0)

//                    val c = Random.pick(human.contours())
//                    val pos = c.position(Random.double0())

                    val pos = Vector2.uniform(
                        bounds.dimensions * Vector2(-0.25, -0.25),
                        bounds.dimensions * Vector2(0.0, 0.25)
                    )
                    val radius = 20.0 + 180.0 * pc.pow(2.5)
                    Circle(pos, radius)
                }

                // TODO:
                // - [x] Add linear repetition of rectangles
                // - [x] quasi symmetry
                // - [ ] Replace Circle with Rectangle. If height == 0, it's a circle
                // - [ ] Create a skeleton and draw shapes in it.

                shapes.forEachIndexed { i, cir ->
                    isolated {
                        fill = if (Random.bool()) ColorRGBa.WHITE else ColorRGBa.BLACK
                        stroke = null
                        if (Random.bool(0.5)) {
                            if (Random.bool(0.75)) {
                                // A. Most times draw the core circle
                                isolated {
                                    circle(cir)
                                    scale(-1.0, 1.0)
                                    circle(cir)
                                }
                            } else {
                                // B. Polar / Cartesian repetition
                                val angleDelta = Random.double(15.0, 60.0)
                                val angleStart = Random.double0(360.0)
                                val radius = Random.double(20.0, 150.0)
                                val copies = Random.int(3, 12)
                                val cartesian = Random.bool(0.4)
                                for (copy in 0 until copies) {
                                    val ang = angleStart + copy * angleDelta
                                    val rect = Rectangle.fromCenter(Vector2.ZERO, cir.radius * 0.5, cir.radius * 0.2)
                                    for (mirror in 0..1) {
                                        isolated {
                                            if (mirror == 1) {
                                                scale(-1.0, 1.0)
                                            }
                                            if (cartesian) {
                                                translate(cir.center + Vector2(0.0, ang - 180))
                                            } else {
                                                translate(cir.center + Polar(ang, radius).cartesian)
                                                rotate(ang)
                                            }
                                            rectangle(rect)
                                        }
                                    }
                                }
                            }
                        } else {
                            for (mirror in 0..1) {
                                // Single rectangle, rotated in multiples of 30°
                                isolated {
                                    if (mirror == 1) {
                                        scale(-1.0, 1.0)
                                    }
                                    translate(cir.center)
                                    rotate(Random.int0(8) * 30.0)
                                    rectangle(Rectangle.fromCenter(Vector2.ZERO, cir.radius * 3.0, cir.radius * 0.5))
                                }
                            }
                        }

                        if (i > 0 && Random.bool(0.3)) {
                            val original = Random.pick(shapes.subList(0, i))
                            var radius = original.radius
                            when (Random.int0(2)) {
                                0 -> {
                                    // re-add white border to possible black circle
                                    stroke = ColorRGBa.WHITE
                                    fill = null
                                    strokeWeight = 6.0
                                }
                                1 -> {
                                    // make whole in the center of previous circle
                                    stroke = null
                                    fill = if (Random.bool(0.7)) ColorRGBa.BLACK else ColorRGBa.WHITE
                                    radius *= Random.double0(0.6)
                                }
                            }
                            isolated {
                                circle(original.center, radius)
                                scale(-1.0, 1.0)
                                circle(original.center, radius)
                            }
                        }
                    }

                }
            }
        }

        fun toCurves() {
            contours.clear()
            val max = 0
            for (i in 0..max) {
                blur.window = map(0.0, max * 1.0, 4.0, 24.0, i * 1.0).toInt()
                blur.sigma = blur.window * 1.0
                blur.apply(withFX, bwBlurred)
                val thres = map(0.0, max * 1.0, 0.45, 0.6, i * 1.0)
                //val thres = 0.3
                contours.addAll(bwBlurred.toContours(thres))
            }
        }

        newImage()

        fun exportSVG() {
            val svg = CompositionDrawer()
            svg.translate(drawer.bounds.center)
            svg.fill = null
            svg.stroke = ColorRGBa.BLACK
            svg.contours(contours)
            saveFileDialog(supportedExtensions = listOf("svg")) {
                it.writeText(writeSVG(svg.composition))
            }
        }

        val actions = @Description("Actions") object {
            @ActionParameter("new image") @Suppress("unused")
            fun doNew() {
                Random.seed = System.currentTimeMillis().toString()
                contours.clear()
                newImage()
            }

            @ActionParameter("bw") @Suppress("unused")
            fun doBW() {
                showBW = !showBW
            }

            @ActionParameter("to curves") @Suppress("unused")
            fun doCreateContours() {
                toCurves()
            }

            @ActionParameter("screenshot") @Suppress("unused")
            fun doScreenshot() {
                screenshots.trigger()
            }

            @ActionParameter("save svg") @Suppress("unused")
            fun doSaveSVG() {
                exportSVG()
            }
        }

        extend(gui) {
            add(actions)
            add(blur)
            add(fx)
        }
        extend(screenshots)
        extend(NoJitter())
        extend {
            drawer.isolated {
                clear(ColorRGBa.WHITE)

                if (showBW) {
                    fx.apply(bw.colorBuffer(0), withFX)
                    //blur.apply(withFX, bwBlurred)
                    image(withFX)
                    //image(bwBlurred)
                } else {
                    stroke = ColorRGBa.BLACK
                    fill = null
                    contours(contours)
                }
            }
        }
        keyboard.keyDown.listen {
            when (it.key) {
                KEY_ESCAPE -> exitProcess(0)
            }
        }
        gui.loadParameters(File("data/parameters/Boofcvbw001.json"))
    }
}
