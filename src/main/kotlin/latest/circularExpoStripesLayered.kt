package latest

import org.openrndr.KEY_ENTER
import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.dialogs.saveFileDialog
import org.openrndr.extra.noise.Random
import org.openrndr.math.Vector2
import org.openrndr.shape.*
import org.openrndr.svg.saveToFile
import kotlin.math.pow

fun main() =
    application {
        configure {
            width = 900
            height = 900
        }
        program {
            Random.seed = System.currentTimeMillis().toString()
            val circle = Circle(Vector2.ZERO, 300.0).shape
            val svg = circle.addStripes(50)
            //svg.draw { shape(circle) }

            extend {
                drawer.clear(ColorRGBa.PINK)
                drawer.translate(drawer.bounds.center)
                drawer.composition(svg)
            }

            mouse.buttonDown.listen {
                svg.draw {
                    val c = Circle(Vector2.ZERO, Random.double(50.0, 300.0))
                        .shape
                    rotate(Random.int0(5) * 72.0)
                    composition(c.addStripes(Random.int(30, 60)))
                }
            }

            keyboard.keyDown.listen {
                when (it.key) {
                    KEY_ENTER -> saveFileDialog(supportedExtensions = listOf("svg")) { file ->
                        svg.saveToFile(file)
                    }
                    KEY_ESCAPE -> application.exit()
                }
            }

        }
    }


private fun Shape.addStripes(num: Int): Composition {
    val shape = this
    return drawComposition {
        fill = null
        val radius = shape.bounds.height / 2
        lineSegments(List(num) {
            val yNorm = (it / (num - 1.0)).pow(2.0)
            val y = (yNorm * 2 - 1) * radius
            val dir = (it % 2) * 2 - 1.0
            LineSegment(-radius * dir, y, radius * dir, y)
        })

        // Cut all the above shapes with the original shape
        clipMode = ClipMode.INTERSECT
        shape(shape)
    }
}
