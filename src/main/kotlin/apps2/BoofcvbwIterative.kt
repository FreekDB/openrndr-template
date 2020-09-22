package apps2

import extensions.NoJitter
import geometry.smoothed
import geometry.toContours
import org.openrndr.KEY_ENTER
import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.dialogs.saveFileDialog
import org.openrndr.draw.*
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.fx.blur.ApproximateGaussianBlur
import org.openrndr.extra.fx.color.ColorCorrection
import org.openrndr.extra.gui.GUI
import org.openrndr.extra.noise.Random
import org.openrndr.extra.parameters.ActionParameter
import org.openrndr.extra.parameters.Description
import org.openrndr.math.Matrix44
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.math.Vector3
import org.openrndr.math.transforms.transform
import org.openrndr.shape.CompositionDrawer
import org.openrndr.shape.Rectangle
import org.openrndr.shape.ShapeContour
import org.openrndr.svg.writeSVG
import kotlin.system.exitProcess


/**
 * BoofCV. Adds shapes one after another to add concentric shapes to the design.
 *
 * Note: Blur .window and .sigma are ignored from the gui as they are set when converting to curves
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
            depthBuffer()
        }.apply {
            clearDepth(0.0)
        }
        val bwBlurred = colorBuffer(width, height)
        val withFX = colorBuffer(width, height)
        val blur = ApproximateGaussianBlur().apply {
            window = 8
            sigma = 8.0
        }
        val fx = ColorCorrection()
        val contours = mutableListOf<ShapeContour>()
        val screenshots = Screenshots()
        val growthPc = MutableList(5) { Random.double0() }
        var growthPositions = List(5) { Vector2.ZERO }

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

        val guiData = @Description("Actions") object {
            @ActionParameter("new")
            fun doNew() {
                contours.clear()
            }

            @ActionParameter("screenshot")
            fun doScreenshot() {
                screenshots.trigger()
            }

            @ActionParameter("save svg")
            fun doSaveSVG() {
                exportSVG()
            }
        }

        fun addShape() {
            val places = mutableListOf<Matrix44>()

            if (Random.bool(0.02)) {
                growthPc[Random.int0(growthPc.size)] = Random.double0()
            }

            if (contours.isEmpty()) {
                Random.seed = System.currentTimeMillis().toString()
                bw.colorBuffer(0).fill(ColorRGBa.BLACK)
                // where to draw next shape
                places.add(transform {
                    translate(drawer.bounds.center)
                })
            } else {
                val neighbors = Random.int(1, 5)
                val lastContour = contours.last()
                val rotOffset = Random.int0(2) * 90.0

                growthPositions = growthPc.map { lastContour.position(it) }
                val minIndex = growthPositions.withIndex().minByOrNull { (_, p) ->
                    (p - mouse.position).squaredLength
                }!!.index
                for (i in 1..neighbors) {
                    val separation = 0.05
                    val location = (growthPc[minIndex] + (i - neighbors/2.0) * separation) % 1.0
                    val pos = lastContour.position(location)
                    val orientation = Polar.fromVector(lastContour.normal(location)).theta
                    // where to draw next shape
                    places.add(transform {
                        translate(pos)
                        rotate(Vector3.UNIT_Z, orientation + rotOffset)
                    })
                }
            }

            drawer.isolatedWithTarget(bw) {
                fill = ColorRGBa.WHITE
                stroke = null

                val shp = Random.int0(2)
                for (place in places) {
                    drawer.isolated {
                        drawer.view *= place
                        when (shp) {
                            0 -> {
                                val sz = if (Random.bool(0.1)) 2.0 else 1.0
                                scale(sz / 2, sz)
                                circle(Vector2.ZERO, Random.double(20.0, 40.0))
                            }
                            1 -> {
                                rectangle(
                                    Rectangle.fromCenter(
                                        Vector2.ZERO,
                                        50 + 10.0 * Random.int0(5), 10.0
                                    )
                                )
                                rectangle(
                                    Rectangle.fromCenter(
                                        Vector2(10.0 * Random.int0(5), Random.double(-5.0, 5.0)),
                                        Random.double(10.0, 30.0), Random.double(15.0, 30.0)
                                    )
                                )
                            }
                        }
                    }
                }
            }

            blur.apply(bw.colorBuffer(0), bwBlurred)
            fx.apply(bwBlurred, withFX)

            withFX.toContours(0.5).filter {
                it.length < width * 1.99 + height * 1.99
            }.map {
                contours.add(it.smoothed(3))
            }

            drawer.isolatedWithTarget(bw) {
                fill = ColorRGBa.WHITE
                stroke = null
                contour(contours.last())
            }
        }

        extend(gui) {
            add(guiData)
            add(blur)
            add(fx)
        }
        extend(screenshots)
        extend(NoJitter())
        extend {
            drawer.isolated {
                clear(ColorRGBa.GRAY)
                stroke = ColorRGBa.BLACK
                fill = null
                contours(contours)
            }
            if (keyboard.pressedKeys.contains("left-alt")) {
                drawer.image(bw.colorBuffer(0), 200.0, 100.0, width / 4.0, height / 4.0)
                drawer.image(bwBlurred, 200.0, 400.0, width / 4.0, height / 4.0)
                drawer.image(withFX, 200.0, 700.0, width / 4.0, height / 4.0)
            }
            drawer.stroke = ColorRGBa.RED
            drawer.fill = null
            drawer.circles(growthPositions, 10.0)
        }
        keyboard.keyDown.listen {
            when (it.key) {
                KEY_ESCAPE -> exitProcess(0)
                KEY_ENTER -> addShape()
            }
        }
        mouse.buttonUp.listen {
            blur.apply(bw.colorBuffer(0), bwBlurred)
            fx.apply(bwBlurred, withFX)
        }
        //gui.loadParameters(File("data/parameters/Boofcvbw001.json"))
    }
}
