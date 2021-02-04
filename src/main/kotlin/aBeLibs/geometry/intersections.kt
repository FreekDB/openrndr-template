package aBeLibs.geometry

import org.openrndr.math.Vector2
import org.openrndr.shape.Circle
import org.openrndr.shape.LineSegment
import org.openrndr.shape.Segment
import org.openrndr.shape.ShapeContour

/**
 * Segment-to-Circle intersection
 * Assumes Segments don't have any control points.
 */
fun Segment.intersections(cir: Circle) = cir.contour.intersections(this)

/**
 * Check for ShapeContour-to-linesegment intersections
 */
fun ShapeContour.intersects(lineSegment: LineSegment) =
    this.intersections(lineSegment.segment)

data class Ray(val start: Vector2, val direction: Vector2)

/**
 * Ray-to-ray intersection
 */
@Suppress("unused")
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

/**
 * Splits a set of line segments on all intersections
 */
fun List<Segment>.chopped(): List<Segment> {
    val result = mutableListOf<Segment>()
    this.forEach { a ->
        val crossings = mutableListOf(0.0)
        // collect all crossings for line a
        this.forEach { b ->
            val intersections = a.intersections(b)
            if (intersections.isNotEmpty()) {
                val t = intersections.first().a.segmentT
                if (t > 0.0 && t < 1.0) {
                    crossings.add(t)
                }
            }
        }
        if (crossings.isEmpty()) {
            // if no crossings add the whole line
            result.add(a)
        } else {
            // if crossings, add all the segments
            crossings.sort()
            for (i in 0 until crossings.size - 1) {
                result.add(
                    Segment(
                        a.position(crossings[i]),
                        a.position(crossings[i + 1])
                    )
                )
            }
        }
    }
    return result
}
