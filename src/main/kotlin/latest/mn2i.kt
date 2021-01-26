package latest

import org.openrndr.application
import org.openrndr.draw.loadImage
import org.openrndr.extensions.Screenshots
import org.openrndr.shape.map

fun main() = application {
    configure {
        width = 1280
        height = 640
    }
    program {
        val img =
            loadImage("/home/funpro/Pictures/n1/Instagram/IMG_20120901_112211.jpg")
        val shadow = img.shadow
        shadow.download()

        extend(Screenshots())
        extend {
            val (x, y) = mouse.position.map(drawer.bounds, img.bounds).toInt()

            drawer.apply {
                val c = shadow[x, y].toHSVa()
                circle(bounds.position(0.25, 0.5), c.h)
                circle(bounds.position(0.50, 0.5), c.s * 100)
                circle(bounds.position(0.75, 0.5), c.v * 100)
            }
        }
    }
}
