package apps.simpleTests

import org.openrndr.applicationSynchronous
import org.openrndr.color.ColorRGBa
import org.openrndr.math.Vector2
import org.openrndr.shape.Segment


fun main() = applicationSynchronous {
    program {
        val segments = List(10) {
            Segment(
                Vector2((it * 1001.0) % width,
                    (it * 1337.0) % height),
                Vector2((it * 3333.0) % width,
                    (it * 5555.0) % height),
                Vector2((it * 6502.0) % width,
                    (it * 4004.0) % height)
            )
        }
        extend {
            drawer.clear(ColorRGBa.WHITE)
            drawer.segments(segments)
        }
    }
}
