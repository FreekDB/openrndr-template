package apps2

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extensions.Screenshots
import org.openrndr.shape.Circle

/**
 * id: 552a3806-3210-482a-bcd2-aee7b4652dca
 * description: Issue: stroke color looks different on contours
 * depending on presence of fill.
 * tags: #new
 */

fun main() = application {
    program {
        val c = Circle(200.0, 200.0, 100.0).contour
        extend(Screenshots())
        extend {
            drawer.run {
                clear(ColorRGBa.WHITE)
                stroke = ColorRGBa.BLACK.opacify(0.5)
                fill = ColorRGBa.WHITE
                contour(c)
                translate(210.0, 0.0)
                fill = null
                contour(c)
            }
        }
    }
}
