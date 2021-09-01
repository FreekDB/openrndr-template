package axi

import aBeLibs.geometry.smoothed
import aBeLibs.geometry.toContours
import org.openrndr.KEY_ENTER
import org.openrndr.application

import org.openrndr.color.ColorRGBa
import org.openrndr.draw.colorBuffer
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.renderTarget
import org.openrndr.extra.fx.blur.ApproximateGaussianBlur
import org.openrndr.extra.noise.Random
import org.openrndr.math.Vector2
import org.openrndr.namedTimestamp
import org.openrndr.shape.Rectangle
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.draw
import org.openrndr.shape.drawComposition
import org.openrndr.svg.saveToFile
import java.io.File

/**
 * BoofCV BW minimal example for converting shapes into curves.
 */

fun main() = application {
    program {
        val svg = drawComposition { }

        val bwBlurred = colorBuffer(width, height)
        val bw = renderTarget(width, height) {
            colorBuffer()
            depthBuffer()
        }.apply {
            clearDepth(0.0)
        }
        val blur = ApproximateGaussianBlur().apply {
            window = 8
            sigma = 8.0
        }

        fun newDesign() {
            val curves = mutableListOf<ShapeContour>()
            drawer.isolatedWithTarget(bw) {
                clear(ColorRGBa.BLACK)
                stroke = null

                val area = drawer.bounds.offsetEdges(-160.0)
                repeat(20) {
                    fill = if (Random.bool())
                        ColorRGBa.WHITE
                    else
                        ColorRGBa.BLACK
                    rectangle(
                        Rectangle.fromCenter(
                            Random.point(area),
                            Random.double(20.0, 150.0)
                        )
                    )
                }
            }

            blur.apply(bw.colorBuffer(0), bwBlurred)

            bwBlurred.toContours(0.5).map {
                curves.add(it.smoothed(5))
            }

            svg.clear()
            svg.draw {
                stroke = ColorRGBa.BLACK
                strokeWeight = 2.0
                fill = ColorRGBa.GRAY
                circle(Vector2.ZERO, 50.0)
                contours(curves)
            }
        }

        extend {
            drawer.clear(ColorRGBa.WHITE)
            drawer.stroke = ColorRGBa.BLUE
            drawer.circle(Vector2.ZERO, 100.0)
            drawer.composition(svg)
        }

        mouse.buttonDown.listen {
            newDesign()
        }
        keyboard.keyDown.listen {
            if (it.key == KEY_ENTER) {
                val fileName = program.namedTimestamp("svg", "print")
                svg.saveToFile(File(fileName))
                println("saved to $fileName")
            }
        }
    }
}
