package apps.p5

import org.openrndr.Fullscreen
import org.openrndr.application
import org.openrndr.color.rgb
import org.openrndr.math.Polar
import org.openrndr.shape.Circle

fun main() = application {
    configure {
        fullscreen = Fullscreen.CURRENT_DISPLAY_MODE
    }
    program {
        val circles = mutableListOf(Circle(0.0, 0.0, 60.0))

        extend {
            drawer.stroke = null
            drawer.fill = rgb(0.2)
            drawer.clear(rgb(0.9))
            drawer.translate(drawer.bounds.center)
            drawer.circles(circles)

            var newPos = Polar(seconds * 5091, width * 1.0).cartesian
            val newRadius = circles.last().radius * 0.98
            var search = true
            while (search) {
                newPos -= newPos.normalized
                circles.lastOrNull { other ->
                    val d = (other.center - newPos).length
                    val minDist = other.radius + newRadius + 5
                    d < minDist
                }?.run {
                    circles.add(Circle(newPos, newRadius))
                    search = false
                }
            }
        }
    }
}
