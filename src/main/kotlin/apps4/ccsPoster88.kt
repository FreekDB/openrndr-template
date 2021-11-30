package apps4

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.loadImage
import org.openrndr.extensions.Screenshots
import org.openrndr.shape.IntRectangle
import java.io.File
import kotlin.math.max
// OPENRNDR 0.4
fun main() = application {
    configure {
        width = 1200
        height = 675
    }
    program {
        val files =
            File("/home/funpro/www/Stammtisch/assets/img/large").listFiles()
        val d = height / files.size.toDouble()
        val slices = files.map {
            val img = loadImage(it)
            img.crop(IntRectangle(0, img.height / 2, img.width, d.toInt()))
        }.sortedBy { slice ->
            var sum = 0.0
            slice.shadow.download()
            for (y in 0 until slice.height) {
                for (x in 0 until slice.width) {
                    val (_, s, v) = slice.shadow[x, y].toHSVa()
                    sum += max(s, 1 - v)
                }
            }
            sum / (slice.width * slice.height)
        }

        extend(Screenshots())
        extend {
            drawer.clear(ColorRGBa.BLACK)
            drawer.stroke = ColorRGBa.BLACK.opacify(0.1)
            slices.forEachIndexed { i, slice ->
                drawer.image(slice, 0.0, i * d, width * 1.0, d)
                drawer.lineSegment(0.0, i * d, width * 1.0, i * d)
            }
        }
    }
}