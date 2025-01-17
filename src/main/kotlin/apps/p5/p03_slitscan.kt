package apps.p5

import org.openrndr.application
import org.openrndr.draw.loadImage
import org.openrndr.draw.shadeStyle
import org.openrndr.extensions.Screenshots

/**
 * id: e1be20e8-dbd4-44b5-b3fe-281d4c37b35e
 * description: New sketch
 * tags: #new
 */

fun main() = application {
    configure {
        width = 1280
        height = 640
    }
    program {
        val img = loadImage("/home/funpro/Pictures/n1/Instagram/IMG_20120901_112211.jpg")
//        val shadow = img.shadow
//        shadow.download()

        val slitStyle = shadeStyle {
            fragmentTransform = "x_fill = texture(p_img, vec2(c_boundsPosition.x, fract(p_time)));"
            parameter("img", img)
        }

        extend(Screenshots())
        extend {
//            val yy = frameCount % img.height
//            for (x in 0 until 50) {
//                val xx = map(0.0, width.toDouble(), 0.0, img.width.toDouble(), x.toDouble()).toInt()
//                drawer.stroke = shadow[xx, yy]
//                drawer.lineSegment(x.toDouble(), 0.0, x.toDouble(), height.toDouble())
//            }
            slitStyle.parameter("time", seconds * 0.05)
            drawer.shadeStyle = slitStyle
            drawer.rectangle(drawer.bounds)
        }
    }
}
