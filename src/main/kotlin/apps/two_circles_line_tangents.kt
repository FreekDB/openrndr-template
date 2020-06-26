package apps

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.math.Vector2
import org.openrndr.shape.Circle
import org.openrndr.shape.LineSegment
import kotlin.math.max
import kotlin.math.sqrt

/**
 * Ported from
 * https://github.com/hamoid/Fun-Programming/tree/master/processing/ideas/2016/07/two_circles_line_tangents
 */

fun main() = application {
    program {
        val c0 = Circle(drawer.bounds.center, 100.0)

        extend {
            val c1 = Circle(mouse.position, 50.0)
            drawer.run {
                fill = null
                clear(ColorRGBa.WHITE)
                circle(c0)
                circle(c1)
                c0.tangents(c1).forEach {
                    lineSegment(it)
                    circle(it.start, 5.0)
                    circle(it.end, 5.0)
                }
            }
        }
    }
}

fun Circle.tangents(other: Circle): List<LineSegment> {
    val result = mutableListOf<LineSegment>()
    val distSq = center.squaredDistanceTo(other.center)
    if (distSq > (radius - other.radius) * (radius - other.radius)) {
        val d = sqrt(distSq)
        val v = (other.center - center) / d
        for (sign1 in arrayOf(1.0, -1.0)) {
            val c = (radius - sign1 * other.radius) / d
            if (c * c <= 1.0) {
                val h = sqrt(max(0.0, 1.0 - c * c))
                for (sign2 in arrayOf(1.0, -1.0)) {
                    val n = v * c + Vector2(-v.y, v.x) * sign2 * h
                    result.add(LineSegment(center + n * radius, other.center + n * sign1 * other.radius))
                }
            }
        }
    }
    return result
}
