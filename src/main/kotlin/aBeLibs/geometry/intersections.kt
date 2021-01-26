package aBeLibs.geometry

import org.openrndr.math.Vector2
import org.openrndr.math.mix
import org.openrndr.shape.*
import kotlin.math.sqrt

/**
 * Segment-to-Segment intersection.
 * Assumes Segments don't have any control points.
 */
fun Segment.intersects(other: Segment, eps: Double = 0.00): Vector2 {
    return intersection(this.start, this.end, other.start, other.end, eps)
}

fun Segment.intersects(start: Vector2, end: Vector2, eps: Double = 0.00): Vector2 {
    return intersection(this.start, this.end, start, end, eps)
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

/**
 * Ray-to-ray intersection
 */
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
 * ShapeContour-to-ShapeContour first intersection
 */
fun ShapeContour.intersects(other: ShapeContour): Vector2 {
    segments.forEach { thisSegment ->
        other.segments.forEach { otherSegment ->
            val p = thisSegment.intersects(otherSegment)
            if (p != Vector2.INFINITY) {
                return p
            }
        }
    }
    return Vector2.INFINITY
}

/**
 * ShapeContour-to-ShapeContour first intersection
 */
@Deprecated("Use built-in ShapeContour.intersections instead")
fun ShapeContour.intersections(other: ShapeContour): List<Vector2> {
    val result = mutableListOf<Vector2>()
    segments.forEach { thisSegment ->
        other.segments.forEach { otherSegment ->
            val p = thisSegment.intersects(otherSegment)
            if (p != Vector2.INFINITY) {
                result.add(p)
            }
        }
    }
    return if(result.isEmpty()) listOf(Vector2.INFINITY) else result
}

/**
 * Check for ShapeContour-to-segment intersections
 */
fun ShapeContour.intersects(otherSeg: Segment): Vector2 {
//    var p = Vector2.INFINITY
//    segments.any { p = it.intersects(segment); p != Vector2.INFINITY }
//    return p
    segments.forEach { thisSeg ->
        val p = thisSeg.intersects(otherSeg)
        // if intersects and not consecutive
        if (p != Vector2.INFINITY && thisSeg.start != otherSeg.end && thisSeg.end != otherSeg.start) {
            return p
        }
    }
    return Vector2.INFINITY
}

/**
 * Check for ShapeContour-to-linesegment intersections
 */
fun ShapeContour.intersects(otherSeg: LineSegment): Vector2 {
    segments.forEach { thisSeg ->
        val p = thisSeg.intersects(otherSeg.start, otherSeg.end)
        // if intersects and not consecutive
        if (p != Vector2.INFINITY && thisSeg.start != otherSeg.end && thisSeg.end != otherSeg.start) {
            return p
        }
    }
    return Vector2.INFINITY
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
