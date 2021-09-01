package apps2

import aBeLibs.geometry.bend
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.LineJoin
import org.openrndr.extra.noise.Random
import org.openrndr.shape.LineSegment
import org.openrndr.shape.Rectangle
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.split

/**
 * Example of splitting ShapeContours with a ShapeContour
 */

fun main() = application {
    program {

        Random.seed = System.currentTimeMillis().toString()

        val lines = mutableListOf<ShapeContour>()
        val center = drawer.bounds.center

        for (w in 150 until 350 step 10) {
            lines.add(Rectangle.fromCenter(center, w * 1.0, w * 1.0).contour)
        }

        val knife = LineSegment(
            drawer.bounds.position(0.0, 0.4),
            drawer.bounds.position(0.5, 0.6)
        ).contour.sampleEquidistant(3)

        val cutLines = lines.map { it.split(knife) }.flatten().bend(knife)

        extend {
            drawer.run {
                clear(ColorRGBa.WHITE)
                fill = null
                stroke = ColorRGBa.GRAY
                lineJoin = LineJoin.BEVEL
                //contour(knife)
                contours(cutLines)
            }
        }
    }
}

