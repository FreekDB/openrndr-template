package apps.simpleTests

import org.openrndr.application
import org.openrndr.draw.*
import org.openrndr.extensions.Screenshots
import org.openrndr.shape.Circle
import kotlin.math.sin

fun main() {
    application {
        program {
            val circle = Circle(0.0, 0.0, 10.0)

            extend(Screenshots())
            extend {
                drawer.stroke = null

                drawer.isolated {
                    translate(bounds.center - 100.0)
                    scale(11.0 + 10 * sin(seconds))
                    shape(circle.shape)
                }

                drawer.isolated {
                    translate(bounds.center + 100.0)
                    scale(11.0 + 10 * sin(seconds))
                    circle(circle)
                }
            }
        }
    }
}
