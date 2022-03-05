package apps.simpleTests

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.noise.Random

/**
 * id: 0c70f565-2c65-4aab-bd26-1cf0d54cf1b9
 * description: New sketch
 * tags: #new
 */

fun main() = application {
    program {
        extend {
            drawer.clear(ColorRGBa.WHITE)
            for (i in 0 until 500) {
                drawer.lineSegment(
                    Random.double0() * width,
                    Random.double0() * height,
                    Random.double0() * width,
                    Random.double0() * height
                )
            }
        }
    }
}
