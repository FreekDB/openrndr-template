package apps2

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.LineJoin
import org.openrndr.draw.shadeStyle
import org.openrndr.math.Polar
import org.openrndr.shape.contour

/**
 * A shadeStyle for dashed lines with two parameters:
 * - dashLength: the length of each dash
 * - dashMargin: the separation between dashes
 *
 * Shows an edge case for morphing lines with segments turning 180 degrees
 */

fun main() = application {
    program {
        val style = shadeStyle {
            fragmentTransform = """
                float l = mod(c_contourPosition, p_dashLength + p_dashMargin);
                x_stroke.a *= 1.0 - step(p_dashLength, l);"""
            parameter("dashLength", 20.0)
            parameter("dashMargin", 5.0)
        }
        extend {
            drawer.run {
                clear(ColorRGBa.WHITE)
                stroke = ColorRGBa.BLACK.opacify(0.5)
                val c = contour {
                    moveTo(100.0, 100.0)
                    continueTo(100.0, 300.0)
                    continueTo(
                        bounds.center + Polar(
                            seconds * 30,
                            100.0
                        ).cartesian
                    )
                    continueTo(500.0, 100.0)
                    continueTo(600.0, 100.0)
                }
                shadeStyle = style
                lineJoin = LineJoin.ROUND
                contour(c)

                lineSegment(0.0, 0.0, width * 1.0, height * 1.0)
            }
        }
    }
}