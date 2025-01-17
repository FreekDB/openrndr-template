package apps

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.MagnifyingFilter
import org.openrndr.draw.colorBuffer
import org.openrndr.extra.noise.Random

/**
 * id: 65da6333-23d9-4fc7-812b-80dfb2ee904e
 * description: Generate a 8x8 pixel black and white pattern
 * and enlarge it (QR code style)
 * tags: #pixel
 */

fun main() = application {
    configure {
        width = 1500
        height = 800
    }

    program {
        val cb = colorBuffer(8, 8)
        cb.filterMag = MagnifyingFilter.NEAREST
        val shad = cb.shadow
        for (y in 0 until cb.height) {
            for (x in 0 until cb.width) {
                shad[x, y] = if (Random.bool()) ColorRGBa.BLACK else ColorRGBa.WHITE
            }
        }
        shad.upload()

        extend {
            drawer.clear(ColorRGBa.WHITE)
            drawer.image(cb, 20.0, 20.0, 400.0, 400.0)
        }
    }
}
