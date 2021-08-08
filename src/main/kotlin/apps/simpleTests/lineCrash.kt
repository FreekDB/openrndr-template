package apps.simpleTests

import org.openrndr.applicationSynchronous
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.noise.Random

fun main() = applicationSynchronous {
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