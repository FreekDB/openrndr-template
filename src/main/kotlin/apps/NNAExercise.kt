package apps

import aBeLibs.extensions.NoJitter
import aBeLibs.geometry.toContours
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.colorBuffer
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.renderTarget
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.fx.blur.ApproximateGaussianBlur
import org.openrndr.extra.gui.GUI
import org.openrndr.extra.noise.Random
import org.openrndr.extra.parameters.ActionParameter
import org.openrndr.extra.parameters.Description
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.math.transforms.transform
import org.openrndr.shape.Circle
import org.openrndr.shape.ShapeContour

/**
 * Description I received:
 *
 * The composition consists of a pair of blobs.
 * The shapes of the blobs are thin and long with a bit of oval shape.
 * The blobs are placed vertically one against each other with an offset space in between.
 * The composition remind a bit to the yin yang symbol The shapes are irregular and have an organic appearance like sort of liquid.
 * The outline of the blob is a bit wavy and have some pronounced areas where the waves have becomes prolonged branches.
 * It looks like they attract / repel each other like magnetic fluid.
 * The top-right shape looks more affected by this "attraction" - "wave prolonged" than the bottom-left. Good luck
 *
 * Impressions: the text describes a specific image instead of defining rules
 *
 * Collaboration with Naoto (author of the image) and Nuño (who described it to me).
 * Presented at the Creative Code Stammtisch on July 3rd 2020
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
        val bwL = renderTarget(width, height) {
            colorBuffer()
            depthBuffer()
        }
        val bwR = renderTarget(width, height) {
            colorBuffer()
            depthBuffer()
        }
        val bwBlurred = colorBuffer(width, height)
        val blur = ApproximateGaussianBlur().apply {
            window = 15
            sigma = 15.0
        }
        val ying = mutableListOf<ShapeContour>()
        val yang = mutableListOf<ShapeContour>()
        val screenshots = Screenshots()

        fun newImage() {
            val baseShapes = List(2) {
                Circle(Vector2.ZERO, 350.0).shape.transform(transform {
                    translate(drawer.bounds.position(0.45 + it * 0.1, 0.5))
                    scale(0.5, 1.0, 1.0)
                })
            }
            arrayOf(bwL, bwR).forEach {
                it.clearColor(0, ColorRGBa.BLACK)
            }

            for (i in 0 until 40) {
                val side = i % 2
                val shp = baseShapes[side]

                val p = Random.gaussian(0.25 + side * 0.5, 0.2)
                val pos = shp.outline.position(p)
                val normal = Polar.fromVector(shp.outline.normal(p))
                val rot = Polar(3.0, 15.0)
                val radius = Random.double(25.0, 45.0)
                val armLength = Random.int(1, 12)
                arrayOf(bwL, bwR).forEachIndexed { idx, rt ->
                    drawer.isolatedWithTarget(rt) {
                        if (side == idx) {
                            stroke = null
                            fill = ColorRGBa.WHITE
                        } else {
                            stroke = ColorRGBa.BLACK
                            fill = ColorRGBa.BLACK
                        }
                        strokeWeight = 8.0
                        when (i) {
                            0, 1 -> shape(shp)
                            else -> {
                                for(rep in 0 until armLength) {
                                    circle(pos + (normal + rot * rep.toDouble()).cartesian, radius - rep)
                                }
                                for(rep in 0 until 10) {
                                    circle(pos - (normal + rot * rep.toDouble()).cartesian, radius - rep)
                                }
                            }
                        }
                    }
                }
            }
        }

        newImage()

        val actions = @Description("Actions") object {
            @ActionParameter("new image")
            fun doNew() {
                Random.seed = mouse.position.toString()
                newImage()

                ying.clear()
                blur.apply(bwL.colorBuffer(0), bwBlurred)
                ying.addAll(bwBlurred.toContours(0.5))

                yang.clear()
                blur.apply(bwR.colorBuffer(0), bwBlurred)
                yang.addAll(bwBlurred.toContours(0.5))
            }

            @ActionParameter("screenshot")
            fun doScreenshot() {
                screenshots.trigger()
            }
        }

        extend(gui) {
            add(actions)
            add(blur)
        }
        extend(screenshots)
        extend(NoJitter())
        extend {
            drawer.run {
                clear(ColorRGBa(0.929, 0.804, 0.518))
                stroke = null

                fill = ColorRGBa(0.925, 0.110, 0.310)
                contours(ying)

                fill = ColorRGBa(0.200, 0.655, 0.525)
                contours(yang)

                if(keyboard.pressedKeys.contains("a")) {
                    image(bwL.colorBuffer(0))
                }
                if(keyboard.pressedKeys.contains("o")) {
                    image(bwR.colorBuffer(0))
                }
            }
        }
    }
}
