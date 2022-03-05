package apps.simpleTests

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle

/**
 * id: a38aea46-799d-4971-a490-70c842831e3d
 * description: New sketch
 * tags: #new
 */

fun main() = application {
    program {
        extend {
            backgroundColor = ColorRGBa.WHITE
            drawer.fill = ColorRGBa.PINK
            drawer.stroke = null
            drawer.circle(drawer.bounds.position(0.0, 0.5), width * 0.5)
            drawer.circle(drawer.bounds.position(1.0, 0.5), width * 0.5)

            drawer.fill = ColorRGBa.GRAY
            drawer.rectangle(Rectangle.fromCenter(Vector2(100.0), 100.0, 100.0))
            drawer.fill = ColorRGBa.WHITE
            drawer.circle(Vector2(100.0), 50.0)
        }
    }
}
