package geometry

import org.openrndr.math.Vector2
import org.openrndr.math.mix
import org.openrndr.shape.Circle
import org.openrndr.shape.Segment
import org.openrndr.shape.intersection
import kotlin.math.sqrt

/**
 * Segment-to-Segment intersection.
 * Assumes Segments don't have any control points.
 */
fun Segment.intersects(other: Segment, eps: Double = 0.00): Vector2 {
    return intersection(this.start, this.end, other.start, other.end, eps)
}

/**
 * Segment-to-Circle intersection
 * Assumes Segments don't have any control points.
 */
fun Segment.intersections(cir: Circle): List<Vector2> {
    val d = end - start
    val f = start - cir.center

    val result = mutableListOf<Vector2>()

    val a = d.dot(d)
    val b = 2 * f.dot(d)
    val c = f.dot(f) - cir.radius * cir.radius

    var discriminant = b * b - 4 * a * c
    if (discriminant < 0) {
        return result
    }

    discriminant = sqrt(discriminant)

    val t1 = (-b - discriminant) / (2 * a)
    val t2 = (-b + discriminant) / (2 * a)

    if (t1 in 0.0..1.0) {
        result.add(mix(start, end, t1))
    }

    if (t2 in 0.0..1.0) {
        result.add(mix(start, end, t2))
    }

    return result
}

data class Ray(val start: Vector2, val direction: Vector2)

fun Ray.intersects(other: Ray): Vector2 {
    val d = other.start - start
    val det = other.direction.x * direction.y - other.direction.y * direction.x
    if (det != 0.0) {
        val u = (d.y * other.direction.x - d.x * other.direction.y) / det
        val v = (d.y * direction.x - d.x * direction.y) / det
        if (u > 0 && v > 0) {
            // front side
            return start + direction * u
        }
        if (u < 0 && v < 0) {
            // backside
            return start + direction * u
        }
    }
    return Vector2.INFINITY // Deal with this later
}