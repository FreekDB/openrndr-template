package latest

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.math.Vector2
import org.openrndr.shape.LineSegment
import org.openrndr.shape.Rectangle

fun main() = application {
    program {
        val square = Rectangle.fromCenter(drawer.bounds.center, 200.0).contour
        val knife = LineSegment(
            Vector2.ZERO,
            drawer.bounds.position(1.0, 1.0)
        ).contour
        val result = square.split(knife).map { it.close }
        extend {
            drawer.apply {
                fill = null
                stroke = ColorRGBa.WHITE
                result.forEach {
                    drawer.contour(it)
                    translate(10.0, 0.0)
                }
            }
        }
    }
}
