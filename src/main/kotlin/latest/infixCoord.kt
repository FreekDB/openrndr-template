package latest

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extensions.Screenshots
import org.openrndr.math.Vector2
import org.openrndr.shape.contour

/**
 * id: 9123d438-98ee-4e49-a4b4-e4367b2d1786
 * description: New sketch
 * tags: #new
 */

fun main() = application {
    configure {
        width = 600
        height = 300
    }
    program {
        val c = contour {
            moveTo(100 x 100)
            curveTo(150 x 50, 200 x 100)
            curveTo(250 x 50, 300 x 100)
            curveTo(200 x 200, 100 x 100)
        }
        extend(Screenshots())
        extend {
            drawer.apply {
                clear(ColorRGBa.PINK)
                contour(c)
            }
        }
    }
}

private infix fun Int.x(y: Int) = Vector2(this.toDouble(), y.toDouble())
