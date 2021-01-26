package latest

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extensions.Screenshots
import org.openrndr.math.Vector2
import org.openrndr.shape.contour

fun main() = application {
    configure {
        width = 1280
        height = 640
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
