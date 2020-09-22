package apps2

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.shadeStyle
import org.openrndr.extensions.Screenshots
import org.openrndr.math.Polar
import org.openrndr.shape.Circle
import org.openrndr.shape.contour

/**
 * Dashed line
 */

fun main() = application {
    program {
        val dashed = shadeStyle {
            fragmentTransform = "x_stroke.a *= step(0.5, fract(c_contourPosition / p_dashLen));"
            //fragmentTransform = "x_stroke.a *= fract(c_contourPosition / p_dashLen);"
            parameter("dashLen", 20.0)
        }
        extend(Screenshots())
        extend {
            drawer.run {
                clear(ColorRGBa.WHITE)
                stroke = ColorRGBa.BLACK.opacify(0.5)
                fill = null
                val c = contour {
                    moveTo(100.0, 100.0)
                    continueTo(100.0, 300.0)
                    continueTo(bounds.center + Polar(seconds * 30, 100.0).cartesian)
                    continueTo(500.0, 100.0)
                    continueTo(600.0, 100.0)
                }
                shadeStyle = dashed
                contour(c)
                contour(Circle(300.0, 300.0, 100.0).contour)
            }
        }
    }
}

