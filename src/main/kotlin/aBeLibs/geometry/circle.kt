package aBeLibs.geometry

import org.openrndr.math.Vector2
import org.openrndr.shape.Circle
import org.openrndr.shape.LineSegment
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

/**
 *
 */
fun Circle.tangentLines(other: Circle): List<LineSegment> {
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

/**
 *
 */
fun Circle.tangentCirclesConvex(other: Circle, tangentRadius: Double): List<Circle> {
    val result = mutableListOf<Circle>()

    val c = this.scaledTo(tangentRadius - radius)
    val d = other.scaledTo(tangentRadius - other.radius)
    c.intersections(d).forEach {
        result.add(Circle(it, tangentRadius))
    }

    return result
}

fun Circle.overlap(other: Circle) = this.center.distanceTo(other.center) < this.radius + other.radius

/**
 *
 */
fun Circle.tangentCirclesConcave(other: Circle, tangentRadius: Double): List<Circle> {
    val result = mutableListOf<Circle>()

    val a = this.scaledTo(radius + tangentRadius)
    val b = other.scaledTo(other.radius + tangentRadius)
    a.intersections(b).forEach {
        result.add(Circle(it, tangentRadius))
    }

    return result
}

/**
 *
 */
fun Circle.intersections(other: Circle): List<Vector2> {
    val diff = other.center - center
    val d = diff.length
    if (d <= radius + other.radius && d > abs(radius - other.radius)) {
        val a = (radius * radius - other.radius * other.radius + d * d) / (2.0 * d)
        val p = center + diff * a / d
        val h = sqrt(radius * radius - a * a)
        val r = Vector2(-diff.y, diff.x) * h / d
        return listOf(p + r, p - r)
    }
    return listOf()
}
