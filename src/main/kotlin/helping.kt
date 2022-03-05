import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolated
import org.openrndr.shape.Circle

/**
 * id: 93cfec77-6ce5-424a-813f-2eb2461d7c1e
 * description: New sketch
 * tags: #new
 */

fun main() = application {
    configure {
        width = 900
        height = 900
    }

    program {
        val c = Circle(drawer.bounds.center, 350.0).contour

        val num = 10
        val parts = List(num) {
            val t = it / num.toDouble()
            c.sub(t, t + 0.9 / num)
        }
        extend {
            drawer.isolated {
                clear(ColorRGBa.WHITE)
                fill = null
                stroke = ColorRGBa.BLACK.opacify(0.6)
                contours(parts)
                parts.map {
                    val t = it.tForLength(it.length * 0.5)
                    val p = it.position(t)
                    val n = it.normal(t)
                    lineSegment(p - n * 50.0, p + n * 50.0)
                }
                stroke = ColorRGBa.BLACK.opacify(0.2)
                parts.forEach {
                    circle(it.position(0.0), it.length * 0.5)
                    circle(it.position(1.0), it.length * 0.5)
                }
            }
        }
    }
}
