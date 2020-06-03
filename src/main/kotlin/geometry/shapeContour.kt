package geometry

import math.cosEnv
import org.openrndr.extra.noise.Random
import org.openrndr.extra.noise.uniformRing
import org.openrndr.math.*
import org.openrndr.shape.Circle
import org.openrndr.shape.Segment
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.contour
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max

// From ofPolyline.inl in openFrameworks
// `smoothingSize` is the size of the smoothing window. So if
// `smoothingSize` is 2, then 2 points from the left, 1 in the center,
// and 2 on the right (5 total) will be used for smoothing each point.
// `smoothingShape` describes whether to use a triangular window (0) or
// box window (1) or something in between (for example, .5).

fun ShapeContour.smoothed(smoothingSize: Int, smoothingShape: Double = 0.0): ShapeContour {
    val n = segments.size
    val sSize = clamp(smoothingSize, 0, n);
    val sShape = clamp(smoothingShape, 0.0, 1.0);

    // precompute weights and normalization
    var weights = List(sSize) {
        map(0.0, sSize * 1.0, 1.0, sShape, it * 1.0)
    }

    val result = MutableList(n) { segments[it].start }

    for (i in 0 until n) {
        var sum = 1.0 // center weight
        for (j in 1 until sSize) {
            var cur = Vector2.ZERO
            var leftPosition = i - j
            var rightPosition = i + j
            if (leftPosition < 0 && closed) {
                leftPosition += n
            }
            if (leftPosition >= 0) {
                cur += segments[leftPosition].start;
                sum += weights[j]
            }
            if (rightPosition >= n && closed) {
                rightPosition -= n
            }
            if (rightPosition < n) {
                cur += segments[rightPosition].start
                sum += weights[j]
            }
            result[i] += cur * weights[j]
        }
        result[i] = result[i] / sum
    }

    return ShapeContour.fromPoints(result, closed)
}

// TODO: have two versions of this? closed one is fine
// but open one should have no distortion at the ends
// and increase in the middle with cosine
fun ShapeContour.noisified(distance: Int, closed: Boolean = true, zoom: Double = 0.002): ShapeContour {
    return ShapeContour.fromPoints(List(this.segments.size + 1) {
        if (it != this.segments.size) {
            val seg = this.segments[it]
            val p = seg.start
            val n = seg.normal(0.0)
            p + n * (Random.perlin(p.x * zoom, p.y * zoom) * distance * 3)
        } else {
            this.segments[it - 1].end
        }
    }, closed)
}

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
 * Split an open ShapeContour with a Circle, erasing what's inside it
 */

fun ShapeContour.split(knife: Circle, resolution: Int = 100): List<ShapeContour> {
    val result = mutableListOf<ShapeContour>()
    val segments = this.equidistantPositions(resolution)
    var points = mutableListOf<Vector2>()
    var last = Vector2.INFINITY
    segments.forEach {
        last = if (knife.contains(it)) {
            if (last != Vector2.INFINITY) {
                // entering circle
            } else {
                // inside circle
            }
            Vector2.INFINITY
        } else {
            if (last == Vector2.INFINITY) {
                //leaving circle
            } else {
                // outside circle
                points.add(it)
            }
            it
        }
    }
    return result
}

/**
 * Split a closed ShapeContour into two with a segment
 */
fun ShapeContour.split(knife: Segment): Pair<ShapeContour, ShapeContour> {
    val points = listOf(mutableListOf<Vector2>(), mutableListOf())
    var which = 0
    var hits = 0
    var previousp = Vector2.INFINITY
    segments.forEach {
        val p = it.intersects(knife)
        points[which].add(it.start)
        // Skip intersections farther away than eps (in normalized distance!)
        // The second test is in case we get the same point twice in a row
        if (p != Vector2.INFINITY && p.distanceTo(previousp) >= 1.0) {
            points[which].add(p)
            which = (which + 1) % 2
            points[which].add(p)
            hits++
            previousp = p
        }
    }
    return if (hits == 2) {
        Pair(
            ShapeContour.fromPoints(points[0], true),
            ShapeContour.fromPoints(points[1], true)
        )
    } else {
        println("Hits = $hits! $this $knife")
        Pair(
            ShapeContour.EMPTY,
            ShapeContour.EMPTY
        )
    }
}

/**
 * Check if a closed ShapeContour contains a point.
 * From openFrameworks
 */
fun ShapeContour.contains(pos: Vector2): Boolean {
    var counter = 0
    var xinters = 0.0
    var n = segments.size
    var p1 = segments[0].start;
    for (i in 1..n) {
        var p2 = segments[i % n].start;
        if (pos.y > kotlin.math.min(p1.y, p2.y)) {
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

/**
 * Find the orientation of the longest segment of a ShapeContour
 */
fun ShapeContour.longestOrientation(): Double {
    val dir = this.longest().direction()
    return Math.toDegrees(atan2(dir.y, dir.x))
}

/**
 * Find the longest segment of a ShapeContour
 */
fun ShapeContour.longest(): Segment {
    return this.segments.maxBy { it.length }!!
}

/**
 * Takes a curve and adds twists in the middle making it more wavy
 */
fun ShapeContour.softJitter(steps: Int, minRadius: Double, maxRadius: Double): ShapeContour {
    val original = this
    return contour {
        moveTo(original.segments.first().start)
        var prev = cursor + Vector2.uniformRing(minRadius, maxRadius) * original.length
        for (i in 1..steps) {
            val pc = i / steps.toDouble()
            val p = original.position(pc)
            val n = original.normal(pc)
            val rotation = Random.double() * maxRadius / 2.22
            val nAngle = Polar.fromVector(n).rotate(rotation - 90.0).cartesian
            val next = nAngle * Random.double(minRadius, maxRadius) * original.length
            curveTo(prev, p + next, p)
            prev = p - next
        }
    }
}

/**
 * Adds localized detours to a curve. Returns a list of curves.
 * Takes a List<Triple<start: Double, end: Double, offset: Double>>
 */
fun ShapeContour.localDistortion(
    data: List<Triple<Double, Double, Double>>,
    steps: Int = 100
): List<ShapeContour> {
    val parts = mutableListOf(this)
    for ((start, end, offset) in data) {
        val seg = this.sub(start, end)
        parts.add(
            ShapeContour.fromPoints(
                List(steps) {
                    val pc = it / (steps - 1.0)
                    val dry = seg.position(pc)
                    val wet = Polar(Random.simplex(dry), offset * cosEnv(pc)).cartesian
                    dry + wet
                }, false
            )
        )
    }
    return parts
}

/**
 * Used to draw a curve with dots at both ends. Use this function to
 * shorten the curve and simulate occlusion by the two circles
 * Example: ( -)-----(- ) becomes (  )------(  )
 */
fun ShapeContour.eraseEndsWithCircles(a: Circle, b: Circle): ShapeContour {
    // Delete the part of a curve inside a circle. Naive approach:
    // 1. Resample it to small segments
    val result = this.sampleLinear(0.5)
    // 2. Find segments intersecting with circles
    val sz = result.segments.size
    val seg0 = result.segments.subList(sz / 2, sz).first { b.contains(it.end) }
    val seg1 = result.segments.subList(0, sz / 2).last { a.contains(it.start) }
    // 3. Find intersection points
    val p0 = seg0.intersections(b)
    val p1 = seg1.intersections(a)
    // 4. Find normalized point on curve for those intersection points
    val u1 = result.on(p0.first(), 1.0) ?: 0.0
    val u0 = result.on(p1.first(), 1.0) ?: 1.0
    // 5. Make new curve skipping start and end (the parts where the curve
    // when into the circles
    return result.sub(u0, u1)
}

/**
 * Creates a variable width contour from a list of Vector3 where
 * `xy` = 2D location
 * `z` = normal length
 */
fun variableWidthContour(points: List<Vector3>): ShapeContour {
    val points2d = points.map { it.xy }

    // TODO: optimize. No need to create a contour to query normals, that was just
    // the easiest. I can calculate the normals directly from points2d

    var i = 1
//    val normals = listOf((points2d[1]-points2d[0]).perpendicular() * points.first().z) +
//            points2d.zipWithNext { p0, p1 -> ((p1-p0).perpendicular()).normalized * points[i++].z } +
//            (points2d.last()-points2d[points2d.size-2]).perpendicular() * points.last().z
//    println("${points2d.size} ${normals.size}")

    // Create a contour we can query.
    val polyline = ShapeContour.fromPoints(points2d, false)

    var normals = listOf(polyline.segments.first().normal(0.0)) +
            polyline.segments.zipWithNext { a, b -> (a.normal(1.0) + b.normal(0.0)).normalized } +
            polyline.segments.last().normal(1.0)

    normals = normals.mapIndexed { i, it -> it * points[i].z }

    // construct shape. First add one side, then the other in reverse.
    return ShapeContour.fromPoints(
        points2d.mapIndexed { i, p ->
            p + normals[i]
        } + points2d.reversed().mapIndexed { i, p ->
            p - normals.reversed()[i]
        }, true
    )
}
