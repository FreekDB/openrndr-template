package apps2

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.color.ColorXSVa
import org.openrndr.extra.shadestyles.NPointGradient
import org.openrndr.math.Polar
import org.openrndr.shape.ShapeContour
import kotlin.math.PI
import kotlin.math.sin

/**
 * Animated closed multi-point gradient
 */

fun main() = application {
    configure {
        width = 1024
        height = 1024
    }
    program {
        val numPoints = 8
        val gradient = NPointGradient(Array(numPoints) {
            ColorXSVa(it * 360.0 / numPoints, 1.0, 1.0).toRGBa()
        })

        extend {
            drawer.run {
                clear(ColorRGBa.WHITE.shade(0.9))
                val t = PI * 2 * (frameCount % 300) / 300.0
                val points = Array(numPoints) {
                    val lfo = sin(it * PI / 2 - t)
                    val theta = it * 360.0 / numPoints + 15 * lfo
                    val radius = 300 + 200 * lfo
                    bounds.center + Polar(theta, radius).cartesian
                }
                gradient.points = points
                shadeStyle = gradient
                stroke = ColorRGBa.WHITE
                strokeWeight = 4.0
                contour(ShapeContour.fromPoints(points.asList(), true))
            }
        }
    }
}
