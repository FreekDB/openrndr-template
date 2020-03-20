package editablecurve

import org.openrndr.math.Vector2
import org.openrndr.shape.Segment
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.intersection
import kotlin.math.PI
import kotlin.math.cos

fun ShapeContour.makeParallelCurve(dist: Double): ShapeContour {
    var points = mutableListOf<Vector2>()
    var prevNorm = Vector2.ZERO
    val len = segments.size.toDouble()
    segments.forEachIndexed { i, it ->
        val pc = i / len
        val wi = 0.5 - 0.5 * cos(pc * PI * 2)
        val norm = (it.end - it.start).normalized.perpendicular
        points.add(it.start + (norm + prevNorm).normalized * wi * dist)
        prevNorm = norm
    }
    points.add(segments.last().end + prevNorm * 0.0 * dist)

    return ShapeContour.fromPoints(points, false)
}

/**
 * Do simple line-to-line intersection test. Assuming
 * the Segments don't have any control points.
 */
fun intersection(a: Segment, b:Segment, eps: Double = 0.01): Vector2 {
    return intersection(a.start, a.end, b.start, b.end, eps)
}

/**
 * Check for ShapeContour-to-segment intersections
 */
fun ShapeContour.intersects(segment: Segment): Vector2 {
    segments.forEach {
        val p = intersection(it, segment)
        if (p != Vector2.INFINITY) {
            return p
        }
    }
    return Vector2.INFINITY
}