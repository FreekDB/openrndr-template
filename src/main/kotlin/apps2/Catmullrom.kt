package apps2

import aBeLibs.geometry.randomPoint
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.uniform
import org.openrndr.math.CatmullRomChain2
import org.openrndr.math.Polar
import org.openrndr.shape.toContour

/**
 * id: d4423268-0f1e-4ebe-9c7b-9cc4c4ffcc07
 * description: Interactive. Mouse position sets alpha for CatmullRom.
 * tags: #new
 */

fun main() = application {
    program {
        // clockwise sorting
        val points = List(9) { drawer.bounds.uniform(100.0) }.sortedBy {
            Polar.fromVector(it - drawer.bounds.center).theta
        }

        extend(Screenshots())
        extend {
            // alpha has no effect when points is evenly distributed in a circle
            val alpha = mouse.position.x / width.toDouble()
            val cmr = CatmullRomChain2(points, alpha, loop = true)
            val contour = cmr.toContour()
            drawer.run {
                clear(ColorRGBa.WHITE)
                fill = null
                stroke = ColorRGBa.BLACK
                contour(contour)
                circles(points, 5.0)

                fill = ColorRGBa.BLACK
                text(alpha.toString(), 50.0, 50.0)
            }
        }
    }
}
