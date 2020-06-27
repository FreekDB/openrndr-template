package apps.simpleTests

import org.openrndr.application
import org.openrndr.color.ColorRGBa

fun main() = application {
    program {
        extend {
            backgroundColor = ColorRGBa.WHITE
            drawer.fill = ColorRGBa.PINK
            drawer.circle(drawer.bounds.position(0.333, 0.333), 60.0)
            drawer.circle(drawer.bounds.position(0.666, 0.333), 60.0)
            drawer.circle(drawer.bounds.position(0.666, 0.666), 60.0)
            drawer.circle(drawer.bounds.position(0.333, 0.666), 60.0)
        }
    }
}
