package apps2.simpleTests

import org.openrndr.application
import org.openrndr.math.Polar

fun main() = application {
    program {

        val c = SimpleDrawingClass(drawer)
        var angle = 0.0

        extend {
            angle += 5.0
            drawer.run {
                drawer.translate(
                    drawer.bounds.center +
                            Polar(angle, 200.0).cartesian
                )
                c.draw()
            }
        }
    }
}
