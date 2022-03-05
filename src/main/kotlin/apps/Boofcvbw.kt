package apps

import aBeLibs.extensions.NoJitter
import aBeLibs.geometry.smoothed
import aBeLibs.geometry.toContours
import aBeLibs.svg.Pattern
import aBeLibs.svg.fill
import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa
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
import org.openrndr.extra.parameters.IntParameter
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.math.map
import org.openrndr.namedTimestamp
import org.openrndr.shape.*
import org.openrndr.svg.saveToFile
import java.io.File
import kotlin.math.pow
import kotlin.system.exitProcess

/**
 * id: 9a51a2a0-c15c-4441-aa1d-470700fdf612
 * description: Creates black and white designs. Applies glitchy effect and blur.
 * Then uses BoofCV to trace contours. Finally, fills contours with patterns.
 * tags: #new
 */

/**
 * Next: create multiple contours. Subtract to remove overlap.
 */

fun main() = application {
    configure {
        width = 1920
        height = 1080
    }
    program {
        val gui = GUI().apply {
            compartmentsCollapsedByDefault = false
        }

        val bw = renderTarget(width, height) {
            colorBuffer()
        }
        val bwBlurred = colorBuffer(width, height)
        val withFX = colorBuffer(width, height)
        val blur = ApproximateGaussianBlur()
        val fx = Perturb()
        val contours = mutableListOf<ShapeContour>()
        val screenshots = Screenshots()
        val svg = drawComposition { }
        val patterns = drawComposition { }

        Random.seed = System.currentTimeMillis().toString()

        val params = @Description("Params") object {
            @IntParameter("steps", 5, 100)
            var steps = 10

            @IntParameter("mirror", 1, 2)
            var mirror = 1
        }

        fun newImage() {
            drawer.isolatedWithTarget(bw) {
                clear(ColorRGBa.BLACK)
                translate(bounds.center)

                val circles = List(params.steps) {
                    val pc =
                        it.toDouble().map(0.0, params.steps - 1.0, 1.0, 0.0)

                    val pos = bounds.sub(-0.25, -0.25, 0.0, 0.25).uniform()
                    val radius = 20.0 + 180.0 * pc.pow(2.5)
                    Circle(pos, radius)
                }

                circles.forEachIndexed { i, cir ->
                    isolated {
                        fill =
                            if (Random.bool()) ColorRGBa.WHITE else ColorRGBa.BLACK
                        stroke = null
                        if (Random.bool(0.5)) {
                            if (Random.bool(0.75)) {
                                // A. Most times draw the core circle
                                isolated {
                                    circle(cir)
                                    if (params.mirror == 2) {
                                        scale(-1.0, 1.0)
                                        circle(cir)
                                    }
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
                                    val rect = Rectangle.fromCenter(
                                        Vector2.ZERO,
                                        cir.radius * 0.5,
                                        cir.radius * 0.2
                                    )
                                    repeat(params.mirror) {
                                        isolated {
                                            if (it == 1) {
                                                scale(-1.0, 1.0)
                                            }
                                            if (cartesian) {
                                                translate(
                                                    cir.center + Vector2(
                                                        0.0, ang - 180
                                                    )
                                                )
                                            } else {
                                                translate(
                                                    cir.center + Polar(
                                                        ang, radius
                                                    ).cartesian
                                                )
                                                rotate(ang)
                                            }
                                            rectangle(rect)
                                        }
                                    }
                                }
                            }
                        } else {
                            repeat(params.mirror) {
                                // Single rectangle, rotated in multiples of 30°
                                isolated {
                                    if (it == 1) {
                                        scale(-1.0, 1.0)
                                    }
                                    translate(cir.center)
                                    rotate(Random.int0(8) * 30.0)
                                    rectangle(
                                        Rectangle.fromCenter(
                                            Vector2.ZERO,
                                            cir.radius * 3.0,
                                            cir.radius * 0.5
                                        )
                                    )
                                }
                            }
                        }

                        if (i > 0 && Random.bool(0.3)) {
                            val original = Random.pick(circles.subList(0, i))
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
                                    fill =
                                        if (Random.bool(0.7)) ColorRGBa.BLACK else ColorRGBa.WHITE
                                    radius *= Random.double0(0.6)
                                }
                            }
                            isolated {
                                circle(original.center, radius)
                                if (params.mirror == 2) {
                                    scale(-1.0, 1.0)
                                    circle(original.center, radius)
                                }
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
                contours.addAll(bwBlurred.toContours(thres).map {
                    it.smoothed(2)
                })
            }

            svg.clear()
            svg.draw {
                fill = null
                stroke = ColorRGBa.BLACK
                contours(contours)
            }
        }

        newImage()

        fun exportSVG() {
            svg.saveToFile(
                File(program.namedTimestamp("svg", "print"))
            )
        }

        fun patternFill() {
            patterns.clear()
            val outline = Shape(contours)
            patterns.fill(outline, Pattern.STRIPES(2.0, 1.0, 30.0))
        }

        val actions = @Description("Actions") object {
            @ActionParameter("new image")
            fun doNew() {
                Random.seed = System.currentTimeMillis().toString()
                contours.clear()
                newImage()
            }

            @ActionParameter("to curves")
            fun doCreateContours() = toCurves()

            @ActionParameter("screenshot")
            fun doScreenshot() = screenshots.trigger()

            @ActionParameter("save svg")
            fun doSaveSVG() = exportSVG()

            @ActionParameter("pattern fill")
            fun doFill() = patternFill()
        }

        extend(gui) {
            add(actions)
            add(params)
            add(blur)
            add(fx)
        }
        extend(screenshots)
        extend(NoJitter())
        extend {
            drawer.isolated {
                clear(ColorRGBa.WHITE)
                if (keyboard.pressedKeys.contains("left-shift")) {
                    fx.apply(bw.colorBuffer(0), withFX)
                    image(withFX)
                } else {
                    composition(svg)
                    composition(patterns)
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
