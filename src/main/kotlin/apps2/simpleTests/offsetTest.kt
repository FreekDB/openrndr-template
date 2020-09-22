package apps2.simpleTests

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extensions.Screenshots
import org.openrndr.math.Vector2
import org.openrndr.shape.SegmentJoin
import org.openrndr.shape.ShapeContour
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

fun main() = application {
    program {

        val points = List(18) {
            val pc = 2 * PI * it / 18.0
            val x = 150.0 * sin(pc + cos(pc + 2))
            val y = 150.0 * cos(pc + 4)
            Vector2(x, y)
        }
        val c = ShapeContour.fromPoints(points, true)
        val c2 = c.offset(30.0, SegmentJoin.MITER)

        println(c.segments.size)
        println(c2.segments.size)

        for(i in 0 until 5) {
            println("${c.segments[i]} --> ${c2.segments[i]}")
        }

        extend(Screenshots())
        extend {
            drawer.run {
                clear(ColorRGBa.WHITE)
                translate(bounds.center)
                fill = null
                contour(c)
                contour(c2)
                circles(c.segments.map { it.start }, 10.0)
                circles(c2.segments.map { it.start }, 10.0)
            }
        }
    }
}
