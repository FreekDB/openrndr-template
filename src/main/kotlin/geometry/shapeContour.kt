package geometry

import boofcv.alg.filter.binary.BinaryImageOps
import boofcv.alg.filter.binary.ThresholdImageOps
import boofcv.struct.ConnectRule
import boofcv.struct.image.GrayS32
import boofcv.struct.image.GrayU8
import math.angle
import math.cosEnv
import math.isAngleReflex
import org.openrndr.animatable.easing.Easing
import org.openrndr.boofcv.binding.toGrayF32
import org.openrndr.boofcv.binding.toVector2s
import org.openrndr.draw.ColorBuffer
import org.openrndr.extra.noise.Random
import org.openrndr.extra.noise.uniformRing
import org.openrndr.math.*
import org.openrndr.shape.Circle
import org.openrndr.shape.Segment
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.contour
import kotlin.math.*

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

fun ShapeContour.noisified(distance: Int, closed: Boolean = true, zoom: Double = 0.002): ShapeContour {
    return ShapeContour.fromPoints(List(this.segments.size + 1) {
        if (it != this.segments.size) {
            val seg = this.segments[it]
            val p = seg.start
            val n = seg.normal(0.0)
            val env = 3 * if (closed) 1.0 else cosEnv(it / (this.segments.size + 1.0))
            p + n * (Random.perlin(p.x * zoom, p.y * zoom) * distance * env)
        } else {
            this.segments[it - 1].end
        }
    }, closed)
}

/**
 *
 */
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
 * Split a ShapeContour with a ShapeContour used as a knife
 */
fun ShapeContour.split(knife: ShapeContour): List<ShapeContour> {
    val result = mutableListOf<ShapeContour>()
    val points = mutableListOf<Vector2>()
    segments.forEach { thisSegment ->
        points.add(thisSegment.start)
        knife.segments.forEach { otherSegment ->

            val intersectionPoint = thisSegment.intersects(otherSegment)
            if (intersectionPoint != Vector2.INFINITY) {
                points.add(intersectionPoint)
                result.add(ShapeContour.fromPoints(points, false))
                points.clear()

                points.add(intersectionPoint)
            }
        }
    }
    if (closed) {
        points.add(position(1.0))
    }
    if (points.size > 0) {
        result.add(ShapeContour.fromPoints(points, false))
        points.clear()
    }
    if (closed && result.size > 1) {
        result[0] = result.last() + result.first()
        result.removeAt(result.size - 1)
    }
    return result
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
            val nAngle = Polar.fromVector(n).rotated(rotation - 90.0).cartesian
            val next = nAngle * Random.double(minRadius, maxRadius) * original.length
            curveTo(prev, p + next, p)
            prev = p - next
        }
    }
}

/**
 * Adds localized detours to a curve. Returns a list of curves.
 * Takes a List<Triple<start: Double, end: Double, offset: Double>>
 * (start percent, end percent, offset)
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
                            val wet = Polar(Random.simplex(dry * 0.005) * 360.0, offset * cosEnv(pc)).cartesian
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

/**
 * Convert a ColorBuffer to contours using boofcv
 */
fun ColorBuffer.toContours(threshold: Double, internal: Boolean = true): List<ShapeContour> {
    val input = this.toGrayF32()

    val binary = GrayU8(input.width, input.height)
    val label = GrayS32(input.width, input.height)
    ThresholdImageOps.threshold(input, binary, threshold.toFloat() * 255, false)
    var filtered = BinaryImageOps.erode8(binary, 1, null)
    filtered = BinaryImageOps.dilate8(filtered, 1, null)
    val contours = BinaryImageOps.contour(filtered, ConnectRule.EIGHT, label)

    val result = mutableListOf<ShapeContour>()
    contours.forEach {
        result.add(ShapeContour.fromPoints(it.external.toVector2s(), true))
        if (internal) {
            it.internal.forEach { internalContour ->
                result.add(ShapeContour.fromPoints(internalContour.toVector2s(), true))
            }
        }
    }
    return result
}

/**
 * Create a shapeContour spiral that starts in `p0`, ends in `p1` and
 * has `center` as center. turns can be positive or negative.
 */
fun spiralContour(p0: Vector2, p1: Vector2, center: Vector2, turns: Int = 1): ShapeContour {
    val polars = arrayOf(
            Polar.fromVector(p0 - center),
            Polar.fromVector(p1 - center)
    )
    val turnsConstrained = if (turns == 0) 1 else turns
    //val clockwise = angleDiff(polars[0].theta, polars[1].theta) < 0
    val minIdx = if (turns < 0) 0 else 1
    val smaller = polars[minIdx]
    var greater = polars[1 - minIdx]
    val minTurn = 160 + (abs(turnsConstrained) - 1) * 360

    while (greater.theta - smaller.theta < minTurn) {
        greater = greater.rotated(360.0)
    }

    val steps = 100 * abs(turns)
    return ShapeContour.fromPoints(List(steps) {
        val lin = it / (steps - 1.0)
        val quad = Easing.SineInOut.easer.ease(lin, 0.0, 1.0, 1.0)
        val theta = mix(smaller.theta, greater.theta, lin)
        val radius = mix(smaller.radius, greater.radius, quad)
        center + Polar(theta, radius).cartesian
    }, false)
}

/**
 *
 */
fun tangentWrapConcave(a: Circle, b: Circle, radius: Double): ShapeContour {
    val points = mutableListOf<Vector2>()
    val tanCircles = a.tangentCirclesConcave(b, radius)
    tanCircles.forEach {
        points.addAll(Segment(a.center, a.center - (a.center - it.center) * a.radius).intersections(a))
        points.addAll(Segment(b.center, b.center - (b.center - it.center) * b.radius).intersections(b))
    }
    if (points.size == 4 && (!tanCircles[0].overlap(tanCircles[1]) || b.overlap(a))) {
        return makeTangentWrapContours(a, b, points, radius, true, false)
    }
    return ShapeContour.EMPTY
}

/**
 *
 */
fun tangentWrapConvex(a: Circle, b: Circle, radius: Double): ShapeContour {
    val points = mutableListOf<Vector2>()
    a.tangentCirclesConvex(b, radius).forEach {
        points.addAll(Segment(a.center, a.center + (a.center - it.center) * a.radius).intersections(a))
        points.addAll(Segment(b.center, b.center + (b.center - it.center) * b.radius).intersections(b))
    }
    if (points.size == 4) {
        return makeTangentWrapContours(a, b, points, radius, false, true)
    }
    return ShapeContour.EMPTY
}

/**
 *
 */
private fun makeTangentWrapContours(
        a: Circle, b: Circle, points: MutableList<Vector2>, radius: Double,
        invertLarge: Boolean, sweepEven: Boolean
): ShapeContour {
    val firstLarge = isAngleReflex(angle(b.center, points[1], points[3])) xor invertLarge
    val secondLarge = !(isAngleReflex(angle(a.center, points[0], points[2])) xor invertLarge)

    return contour {
        moveTo(points[0])
        arcTo(radius, radius, 0.0, largeArcFlag = false, sweepFlag = true, end = points[1])
        arcTo(b.radius, b.radius, 0.0, largeArcFlag = firstLarge, sweepFlag = sweepEven, end = points[3])
        arcTo(radius, radius, 0.0, largeArcFlag = false, sweepFlag = true, end = points[2])
        arcTo(a.radius, a.radius, 0.0, largeArcFlag = secondLarge, sweepFlag = sweepEven, end = points[0])
        close()
    }
}

fun List<ShapeContour>.bend(knife: ShapeContour) = this.map { contour ->
    val points = contour.equidistantPositions(contour.length.toInt())
            .toMutableList()

    if(points.isEmpty()) {
        return@map contour
    }

    // TODO: remove hardcoded 50. Should be an argument.
    // Or make it based on distance as I tried earlier.
    // So effect strength increases with distance to cut tip and
    // decreases with distance to cut-line.
    val pointCount = min(points.size, 50)
    if(knife.on(points.first(), 1.0) != null) {
        for (i in 0 until pointCount) {
            val origin = points[i].copy()
            val f = Random.simplex(points[i] * 0.03) * 200.0
//            val f = j.toDouble().map(i * 1.0, 0.0, 0.0, 1.0)
//                    .pow(2.0) * 10.0
            //val f = 1.0 / (d * 0.2)
            val nearest = knife.nearest(points[i])
            val d = nearest.position.distanceTo(points[i])
            val n = nearest.contourT
            for (j in i downTo 0) {
                points[j] = points[j].rotate(f / (d + 1), origin)
            }
        }
    }
    if(knife.on(points.last(), 1.0) != null) {
        for (i in points.size - pointCount until points.size - 1) {
            val origin = points[i].copy()
            val f = -Random.simplex(points[i] * 0.03) * 200.0
//            val f = j.toDouble().map(i2 * 1.0, points.size - 1.0, 0.0, 1.0)
//                    .pow(2.0) * 10.0
            //val f = -1.0 / (d * 0.2)
            val nearest = knife.nearest(points[i])
            val d = nearest.position.distanceTo(points[i])
            val n = nearest.contourT
            for (j in i until points.size) {
                points[j] = points[j].rotate(f / (d + 1), origin)
            }
        }
    }

    ShapeContour.fromPoints(points, false)
}

fun ShapeContour.onAll(point: Vector2, error: Double = 5.0): List<Double> {
    val result = mutableListOf<Double>()
    for (i in segments.indices) {
        val st = segments[i].on(point, error)
        if (st != null) {
            result.add((i + st) / segments.size)
        }
    }
    return result
}

fun ShapeContour.selfIntersections(): List<Double> {
    val result = mutableListOf<Double>()
    val intersections = mutableListOf<Vector2>()
    this.segments.forEachIndexed { i, seg ->
        val p = this.intersects(seg)
        if (p != Vector2.INFINITY) {
            intersections.add(p)
        }
    }
    intersections.distinctBy { it.x.toInt() * 5000 + it.y.toInt() }.forEach { p ->
        result.add(this.onAll(p, 1.0).random())
    }
    return result.sorted()
}

fun ShapeContour.shorten(d: Double): ShapeContour {
    val step = 1.0 / length
    var startPc = 0.0
    var endPc = 1.0
    val start = position(startPc)
    val end = position(endPc)
    while (start.distanceTo(position(startPc)) < d && startPc < 0.4) {
        startPc += step
    }
    while (end.distanceTo(position(endPc)) < d && endPc > 0.6) {
        endPc -= step
    }
    return sub(startPc, endPc)
}

fun ShapeContour.removeSelfIntersections(margin: Double = 10.0):
        List<ShapeContour> {
    // find all self intersections (normalized positions in the curve)
    val intPcs = selfIntersections()

    // Show found intersections
//                intPcs.forEachIndexed { i, it ->
//                    println("$i -> $it")
//                    val p = uncut.position(it)
//                    svg.lineSegment(p - Vector2(5.0, 0.0),
//                            p + Vector2(5.0, 0.0))
//                    svg.lineSegment(p - Vector2(0.0, 5.0),
//                            p + Vector2(0.0, 5.0))
//                }

    println("Intersections: ${intPcs.size}")
    if (intPcs.isNotEmpty()) {
        val result = mutableListOf<ShapeContour>()
        for (index in intPcs.indices) {
            if (index < intPcs.size - 1) {
                result.add(sub(intPcs[index],
                        intPcs[index + 1]).shorten(margin))
            } else {
                // Last one crosses the curve starting point
                // LAST .. END + START .. FIRST
                result.add((sub(intPcs[index], 1.0) +
                        sub(0.0, intPcs[0])).shorten(margin))
            }
        }
        return result
    } else {
        return listOf(this)
    }
}

fun MutableList<ShapeContour>.removeIntersections(margin: Double):
        List<ShapeContour> {
    var intersectionsFound: Boolean
    main@ do {
        intersectionsFound = false
        for (aIndex in 0 until size - 1) {
            val a = this[aIndex]
            for (bIndex in aIndex + 1 until size) {
                val b = this[bIndex]
                val intersections = a.intersections(b)
                if (intersections[0] != Vector2.INFINITY) {
                    val parts = listOf(
                            a.split(b).toMutableList(),
                            b.split(a).toMutableList()
                    )
                    removeAll(listOf(a, b))
                    intersections.forEach { intersection ->
                        val part = parts.random(Random.rnd)
                        part.forEachIndexed { i, c ->
                            val cutLen = margin / c.length
                            c.onAll(intersection, 1.0).forEach {
                                if (it < 0.2) {
                                    part[i] = c.sub(cutLen, 1.0)
                                } else if (it > 0.8) {
                                    part[i] = c.sub(0.0, 1.0 - cutLen)
                                }
                            }
                        }
                    }

                    addAll(parts.flatten())
                    intersectionsFound = true
                    continue@main
                }
            }
        }
    } while (intersectionsFound)
    return this
}
