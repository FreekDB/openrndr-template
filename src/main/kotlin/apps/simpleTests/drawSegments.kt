package apps.simpleTests

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.math.Vector2
import org.openrndr.shape.Segment

/**
 * id: ae548fe7-d038-4909-822c-c8f761be7aba
 * description: New sketch
 * tags: #new
 */

fun main() = application {
    program {
        val segments = List(10) {
            Segment(
                Vector2(
                    (it * 1001.0) % width,
                    (it * 1337.0) % height
                ),
                Vector2(
                    (it * 3333.0) % width,
                    (it * 5555.0) % height
                ),
                Vector2(
                    (it * 6502.0) % width,
                    (it * 4004.0) % height
                )
            )
        }
        extend {
            drawer.clear(ColorRGBa.WHITE)
            drawer.segments(segments)
        }
    }
}
