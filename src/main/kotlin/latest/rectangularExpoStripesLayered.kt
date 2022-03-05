package latest

import org.openrndr.*
import org.openrndr.color.ColorRGBa
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.Random
import org.openrndr.math.Vector2
import org.openrndr.shape.*
import org.openrndr.svg.saveToFile
import java.io.File
import kotlin.math.pow

/**
 * id: 49f40bd6-5d6a-49c4-87a3-1f0d5e8f81c4
 * description: New sketch
 * tags: #new
 */

fun main() = application {
    configure {
        width = 900
        height = 900
    }
    program {
        Random.seed = System.currentTimeMillis().toString()

        val svg = drawComposition { }

        fun addLayer() {
            svg.draw {
                //val shp = Circle(Vector2.ZERO, 50.0 rnd 300.0).shape
                val shp = Rectangle.fromCenter(
                    Vector2.ZERO,
                    50.0 rnd 600.0, 50.0 rnd 600.0
                ).shape
                //rotate((0 rnd 4) * 90.0)
                rotate(0.0 rnd 360.0)
                val num = 30 rnd 60
                val radius = shp.bounds.height / 2
                val expo = 5.42
                val pattern = shp.addPattern {
                    lineSegments(List(num) {
                        val yNorm = (it / (num - 1.0)).pow(expo)
                        val y = (yNorm * 2 - 1) * radius
                        val alternate = (it % 2) * 2 - 1.0
                        val start = Vector2(-radius * alternate, y)
                        val end = Vector2(radius * alternate, y)
                        LineSegment(start, end)
                    })
                }
                composition(pattern)
            }
        }

        addLayer()

        extend(Screenshots())
        extend {
            drawer.clear(ColorRGBa.PINK)
            drawer.translate(drawer.bounds.center)
            drawer.composition(svg)
        }

        mouse.buttonDown.listen {
            addLayer()
        }

        keyboard.keyDown.listen {
            when (it.key) {
                KEY_ENTER -> svg.saveToFile(
                    File(
                        program.namedTimestamp("svg", "print")
                    )
                )
                KEY_ESCAPE -> application.exit()
                KEY_DELETE -> svg.clear()
            }
        }

    }
}


private infix fun Double.rnd(max: Double) = Random.double(this, max)
private infix fun Int.rnd(max: Int) = Random.int(this, max)

private fun Shape.addPattern(pattern: CompositionDrawer.() -> Unit): Composition {
    val cutter = this
    // Clip pattern using the cutter shape
    return drawComposition {
        pattern()
        clipMode = ClipMode.INTERSECT
        shape(cutter)
    }
}
