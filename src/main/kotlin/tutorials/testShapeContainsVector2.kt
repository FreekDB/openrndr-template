import aBeLibs.geometry.circleish
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolated
import org.openrndr.extensions.Screenshots
import org.openrndr.math.Vector2
import org.openrndr.shape.LineSegment
import org.openrndr.shape.Shape
import org.openrndr.shape.contains

/**
 * Shows that Shape.contains(Vector2) does take into account holes
 * if they have the correct winding (use .reversed!)
 */

fun main() = application {
    program {
        val s = Shape(
            listOf(
                circleish(drawer.bounds.center, 200.0).contour,
                circleish(drawer.bounds.center, 100.0).contour.reversed
            )
        )
        val points = LineSegment(Vector2.ZERO, drawer.bounds.dimensions)
            .sub(0.1, 0.9).contour.equidistantPositions(20)
        val insidePoints = points.filter { s.contains(it) }

        extend(Screenshots())
        extend {
            drawer.isolated {
                clear(ColorRGBa.WHITE)
                stroke = null
                fill = ColorRGBa.GRAY
                shape(s)
                fill = ColorRGBa.GREEN
                circles(insidePoints, 10.0)
                fill = ColorRGBa.PINK
                circles(points, 6.0)
            }
        }
    }
}