package editablecurve

import org.openrndr.math.Vector2
import org.openrndr.shape.Segment
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.intersection
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min

fun ShapeContour.makeParallelCurve(dist: Double): ShapeContour {
    var points = mutableListOf<Vector2>()
    var prevNorm = Vector2.ZERO
    val len = segments.size.toDouble()
    segments.forEachIndexed { i, it ->
        val pc = i / len
        val wi = 0.5 - 0.5 * cos(pc * PI * 2)
        val norm = (it.end - it.start).normalized.perpendicular()
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
fun intersection(a: Segment, b: Segment, eps: Double = 0.01): Vector2 {
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

/**
 * Split a ShapeContour into two with a segment
 */
fun ShapeContour.split(segment: Segment): Pair<ShapeContour, ShapeContour> {
    val a = listOf(mutableListOf<Vector2>(), mutableListOf())
    var which = 0
    var hits = 0
    segments.forEach {
        val p = intersection(it, segment)
        a[which].add(it.start)
        if (p != Vector2.INFINITY) {
            a[which].add(p)
            which = (which + 1) % 2
            a[which].add(p)
            hits++
        }
    }
    if(hits != 2) {
        println("Hits != 2!")
    }
    return Pair(ShapeContour.fromPoints(a[0], true), ShapeContour.fromPoints(a[1], true))
}

/**
 * Check for ShapeContour-to-ShapeContour intersections
 */
fun ShapeContour.intersects(other: ShapeContour): Vector2 {
    segments.forEach { thisSegment ->
        other.segments.forEach { otherSegment ->
            val p = intersection(thisSegment, otherSegment)
            if (p != Vector2.INFINITY) {
                return p
            }
        }
    }
    return Vector2.INFINITY
}

/**
 * Check if ShapeContour contains a point.
 * From openFrameworks
 */
fun ShapeContour.contains(pos: Vector2): Boolean {
    var counter = 0
    var xinters = 0.0
    var n = segments.size
    var p1 = segments[0].start;
    for (i in 1..n) {
        var p2 = segments[i % n].start;
        if (pos.y > min(p1.y, p2.y)) {
            if (pos.y <= max(p1.y, p2.y)) {
                if (pos.x <= max(p1.x, p2.x)) {
                    if (p1.y != p2.y) {
                        xinters = (pos.y - p1.y) * (p2.x - p1.x) / (p2.y - p1.y) + p1.x
                        if (p1.x == p2.x || pos.x <= xinters)
                            counter++
                    }
                }
            }
        }
        p1 = p2;
    }
    return counter % 2 != 0;
}