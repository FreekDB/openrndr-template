package latest


import org.openrndr.application
import org.openrndr.color.rgb
import org.openrndr.extensions.Screenshots
import org.openrndr.math.Vector2
import org.openrndr.shape.LineSegment
import org.openrndr.shape.Rectangle
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.split

/**
 * Split a square with a straight line,
 * then .close the two halves of the square with a straight segment.
 */
fun main() = application {
    program {
        val square = Rectangle.fromCenter(drawer.bounds.center, 200.0).contour
        val knife = LineSegment(Vector2.ZERO, drawer.bounds.dimensions).contour
        val result = (square / knife).map { it.close() }
        extend(Screenshots())
        extend {
            drawer.apply {
                clear(rgb("CAC5B9"))
                stroke = rgb("3E3D4A")
                fill = stroke?.opacify(0.1)
                result.forEach {
                    contour(it)
                    rotate(-3.0)
                }
            }
        }
    }
}

private operator fun ShapeContour.div(knife: ShapeContour) = this.split(knife)
