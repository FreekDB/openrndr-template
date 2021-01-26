package aBeLibs.geometry

import org.openrndr.extra.noise.Random
import org.openrndr.math.Vector2
import org.openrndr.math.mix
import org.openrndr.shape.LineSegment
import org.openrndr.shape.Rectangle
import kotlin.math.*

fun fromIrregularLine(p0: Vector2, p1: Vector2, pc: Double, time: Double = 0.0): Vector2 {
    val offset = cos(pc * PI * 2 - PI) * 0.5 + 0.5
    val distance = (p0 - p1).length * 0.15
    val point = mix(p0, p1, pc)
    return point + Vector2(
        distance * Random.simplex(point.x * 0.01 - time, point.y * 0.01) * offset,
        distance * Random.simplex(point.y * 0.01 + time, point.x * 0.01) * offset
    )
}

/** calculates [Rectangle]-bounds for a list of [LineSegment] instances */
fun lineSegmentBounds(lines: List<LineSegment>): Rectangle {
    var minX = Double.POSITIVE_INFINITY
    var minY = Double.POSITIVE_INFINITY
    var maxX = Double.NEGATIVE_INFINITY
    var maxY = Double.NEGATIVE_INFINITY

    lines.forEach {
        minX = min(minX, min(it.start.x, it.end.x))
        maxX = max(maxX, max(it.start.x, it.end.x))
        minY = min(minY, min(it.start.y, it.end.y))
        maxY = max(maxY, max(it.start.y, it.end.y))
    }
    return Rectangle(Vector2(minX, minY), maxX - minX, maxY - minY)
}
