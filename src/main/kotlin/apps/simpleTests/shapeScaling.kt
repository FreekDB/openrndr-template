package apps.simpleTests

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.extensions.Screenshots
import org.openrndr.shape.Circle
import org.openrndr.shape.Rectangle
import kotlin.math.sin

fun main() =
    application {
        program {
            val circle = Circle(0.0, 0.0, 10.0)

            extend(Screenshots())
            extend {
                drawer.stroke = null
                val k = 11.0 + 10 * sin(seconds)

                drawer.isolated {
                    translate(bounds.position(0.2, 0.5))
                    scale(k)
                    shape(circle.shape)
                }

                drawer.isolated {
                    translate(bounds.position(0.8, 0.5))
                    scale(k)
                    circle(circle)
                }

                drawer.isolated {
                    stroke = ColorRGBa.WHITE
                    fill = null
                    rectangle(Rectangle.fromCenter(bounds.center, 2000.0, 2 * 10.0 * k))
                }
            }
        }
    }
