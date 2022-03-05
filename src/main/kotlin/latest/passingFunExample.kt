package latest

import aBeLibs.extensions.ClipboardScreenshot
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.LineJoin
import org.openrndr.extra.noise.Random
import org.openrndr.extras.color.presets.WHEAT
import org.openrndr.shape.Circle
import org.openrndr.shape.ShapeContour
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin

/**
 * id: 16129e27-e39e-4f0b-ba9e-4d043835f829
 * description: New sketch
 * tags: #new
 */

fun main() = application {
    configure {
        width = 900
        height = 900
    }
    program {
        val contours = mutableListOf<ShapeContour>()
        contours.add(Circle(drawer.bounds.center, 320.0).contour)
        repeat(25) {
            val dist = (it * 1.5 + 1.0).pow(1.4)
            contours.add(contours.first().offset(500) { n ->
                (Random.simplex(sin(n * PI * 6 + it * 0.1), 0.0) - 0.2) * dist
            })
        }

        extend(ClipboardScreenshot())
        extend {
            drawer.apply {
                clear(ColorRGBa.WHEAT)
                stroke = ColorRGBa.BLACK.opacify(0.8)
                fill = null
                lineJoin = LineJoin.ROUND
                contours(contours)
            }
        }
    }
}

private fun ShapeContour.offset(pointCount: Int, amt: Double) =
    ShapeContour.fromPoints(
        equidistantPositions(pointCount).mapIndexed { i, it ->
            val pc = i / pointCount.toDouble()
            it + normal(pc) * amt
        }, closed
    )

private fun ShapeContour.offset(pointCount: Int, amt: (Double) -> Double) =
    ShapeContour.fromPoints(
        equidistantPositions(pointCount).mapIndexed { i, it ->
            val pc = i / pointCount.toDouble()
            it + normal(pc) * amt(pc)
        }, closed
    )