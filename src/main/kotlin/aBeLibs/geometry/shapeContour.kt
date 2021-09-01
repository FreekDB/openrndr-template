package aBeLibs.geometry

import aBeLibs.math.angle
import aBeLibs.math.cosEnv
import aBeLibs.math.isAngleReflex
import boofcv.alg.filter.binary.BinaryImageOps
import boofcv.alg.filter.binary.ThresholdImageOps
import boofcv.struct.ConnectRule
import boofcv.struct.image.GrayS32
import boofcv.struct.image.GrayU8
import org.openrndr.animatable.easing.Easing
import org.openrndr.boofcv.binding.toGrayF32
import org.openrndr.boofcv.binding.toVector2s
import org.openrndr.draw.ColorBuffer
import org.openrndr.extra.noise.Random
import org.openrndr.extra.noise.uniformRing
import org.openrndr.math.*
import org.openrndr.shape.*
import kotlin.math.*

/**
 * Returns a smoothed version of a contour.
 * From openFrameworks ofPolyline.inl
 *
 * @param smoothingSize Size of the smoothing window. A value of 2 means two
 * to the left and two to the right (smoothing 5 values in total)
 * @param smoothingShape A normalized value where 0 means triangular window,
 * 1 means box window, and other values are a mix of the two.
 */
fun ShapeContour.smoothed(
    smoothingSize: Int,
    smoothingShape: Double = 0.0
): ShapeContour {
    val n = segments.size
    val sSize = clamp(smoothingSize, 0, n)
    val sShape = clamp(smoothingShape, 0.0, 1.0)

    // precompute weights and normalization
    val weights = List(sSize) {
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
                cur += segments[leftPosition].start
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

/**
 * Adds noise to a shape contour
 *
 * @param distance How far should vertices be pushed
 * @param closed Should the returned shape be closed
 * @param zoom The scale of the simplex noise. Higher values = noisier results
 */
fun ShapeContour.noisified(
    distance: Double,
    closed: Boolean = true,
    zoom: Double = 0.002
): ShapeContour {
    return ShapeContour.fromPoints(this.segments.mapIndexed { i, it ->
        val p = it.start
        val n = it.normal(0.0)
        val noise = Random.simplex(p * zoom)
        val env = if (closed) 1.0 else cosEnv(i / (this.segments.size - 1.0))
        p + n * (3.0 * distance * noise * env)
    }, closed)
}

/**
 * Beautifies a [ShapeContour] after calling [ShapeContour.offset]
 * which may introduce artifacts and loops in certain locations
 */
fun ShapeContour.beautify(): ShapeContour {
    val equi = sampleEquidistant(segments.size / 2)
    val smooth = equi.smoothed(2)

    val equi2 = smooth.equidistantPositions(smooth.segments.size)
    val smoother = chaikinSmooth(equi2, 1, closed, 0.2)

    return ShapeContour.fromPoints(smoother, closed)
}

/**
 * Similar to [ShapeContour.offset] but with variable offset.
 *
 * [offset] is a function that returns a distance based on the normalized
 * position on the curve. The default offset does fade-in-out using cosine.
 */
fun ShapeContour.makeParallelCurve(
    offset: (Double) -> Double = { pc -> 0.5 - 0.5 * cos(pc * PI * 2) }
): ShapeContour {
    val points = mutableListOf<Vector2>()
    var prevNorm = Vector2.ZERO
    val len = segments.size.toDouble()
    segments.forEachIndexed { i, it ->
        val norm = (it.end - it.start).normalized.perpendicular()
        points.add(it.start + (norm + prevNorm).normalized * offset(i / len))
        prevNorm = norm
    }
    if (!closed) {
        points.add(segments.last().end + prevNorm * offset(1.0))
    }

    return ShapeContour.fromPoints(points, closed)
}

/**
 * Find the orientation of the longest segment of a ShapeContour
 */
@Suppress("unused")
fun ShapeContour.longestOrientation(): Double {
    val dir = longest().direction()
    return Math.toDegrees(atan2(dir.y, dir.x))
}

/**
 * Find the longest segment of a ShapeContour
 */
fun ShapeContour.longest(): Segment {
    return segments.maxByOrNull { it.length }!!
}

/**
 * Adds twists to a [ShapeContour] making it more wavy
 *
 * @param steps Point count for the new contour
 * @param minRadius Min radius for a [Vector2.uniformRing]
 * @param maxRadius Max radius for a [Vector2.uniformRing]
 */
fun ShapeContour.softJitter(
    steps: Int,
    minRadius: Double,
    maxRadius: Double
): ShapeContour {
    val original = this
    return contour {
        moveTo(original.segments.first().start)
        var prev =
            cursor + Vector2.uniformRing(minRadius, maxRadius) * original.length
        for (i in 1..steps) {
            val pc = i / steps.toDouble()
            val p = original.position(pc)
            val n = original.normal(pc)
            val rotation = Random.double() * maxRadius / 2.22
            val nAngle = Polar.fromVector(n).rotated(rotation - 90.0).cartesian
            val next =
                nAngle * Random.double(minRadius, maxRadius) * original.length
            curveTo(prev, p + next, p)
            prev = p - next
        }
    }
}

/**
 * Adds localized simplex-based detours to a curve.
 * [data] contains triplets with
 * - curve start percent
 * - curve end percent
 * - max offset
 * The number of entries in data control how many [ShapeContour]s are created.
 * The localized detour follows a cosine envelope (basically a fade-in-out)
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
                    val wet = Polar(
                        Random.simplex(dry * 0.005) * 360.0,
                        offset * cosEnv(pc)
                    ).cartesian
                    dry + wet
                }, false
            )
        )
    }
    return parts
}

/**
 * Used to draw a curve with dots at both ends. Use this function to
 * shorten the curve and simulate occlusion by the [a] and [b] circles.
 * Example: ( -)-----(- ) becomes (  )------(  )
 */
fun ShapeContour.eraseEndsWithCircles(a: Circle, b: Circle) =
    difference(difference(this, a.shape), b.shape).contours.first()

/**
 * Converts an open [ShapeContour] into a closed one having width.
 * The [points] list specifies the 2D locations of the points plus the
 * desired thickness for each, encoded as the .z component.
 */
fun variableWidthContour(points: List<Vector3>): ShapeContour {
    val points2d = points.map { it.xy }

    // TODO: optimize. No need to create a contour to query normals, that was just
    // the easiest. I can calculate the normals directly from points2d

//    var i = 1
//    val normals = listOf((points2d[1]-points2d[0]).perpendicular() * points.first().z) +
//            points2d.zipWithNext { p0, p1 -> ((p1-p0).perpendicular()).normalized * points[i++].z } +
//            (points2d.last()-points2d[points2d.size-2]).perpendicular() * points.last().z
//    println("${points2d.size} ${normals.size}")

    // Create a contour we can query.
    val polyline = ShapeContour.fromPoints(points2d, false)

    var normals = listOf(polyline.segments.first().normal(0.0)) +
            polyline.segments.zipWithNext { a, b ->
                (a.normal(1.0) + b.normal(
                    0.0
                )).normalized
            } +
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
 * Convert a [ColorBuffer] to contours using boofcv. The normalized
 * [threshold] value specifies the brightness cutoff point. [internal]
 * specifies whether internal shapes should be added too.
 */
fun ColorBuffer.toContours(
    threshold: Double,
    internal: Boolean = true
): List<ShapeContour> {
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
                result.add(
                    ShapeContour.fromPoints(
                        internalContour.toVector2s(),
                        true
                    )
                )
            }
        }
    }
    return result
}

/**
 * Create a [ShapeContour] spiral that starts in [p0], ends in [p1] and
 * is centered in [center]. [turns] can be positive or negative.
 */
fun spiralContour(
    p0: Vector2,
    p1: Vector2,
    center: Vector2,
    turns: Int = 1
): ShapeContour {
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
    tanCircles.forEach { tanCircle ->
        points.addAll(
            Segment(
                a.center,
                a.center - (a.center - tanCircle.center) * a.radius
            ).intersections(a.contour).map { it.position }
        )
        points.addAll(
            Segment(
                b.center,
                b.center - (b.center - tanCircle.center) * b.radius
            ).intersections(b.contour).map { it.position }
        )
    }
    if (points.size == 4 && (!tanCircles[0].overlap(tanCircles[1]) || b.overlap(
            a
        ))
    ) {
        return makeTangentWrapContours(
            a, b, points, radius,
            invertLarge = true,
            sweepEven = false
        )
    }
    return ShapeContour.EMPTY
}

/**
 *
 */
@Suppress("unused")
fun tangentWrapConvex(a: Circle, b: Circle, radius: Double): ShapeContour {
    val points = mutableListOf<Vector2>()
    a.tangentCirclesConvex(b, radius).forEach { circle ->
        points.addAll(
            Segment(
                a.center,
                a.center + (a.center - circle.center) * a.radius
            ).contour.intersections(a.contour).map { it.position }
        )
        points.addAll(
            Segment(
                b.center,
                b.center + (b.center - circle.center) * b.radius
            ).contour.intersections(b.contour).map { it.position }
        )
    }
    if (points.size == 4) {
        return makeTangentWrapContours(
            a, b, points, radius,
            invertLarge = false,
            sweepEven = true
        )
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
    val firstLarge =
        isAngleReflex(angle(b.center, points[1], points[3])) xor invertLarge
    val secondLarge =
        !(isAngleReflex(angle(a.center, points[0], points[2])) xor invertLarge)

    return contour {
        moveTo(points[0])
        arcTo(
            radius, radius, 0.0,
            largeArcFlag = false, sweepFlag = true, end = points[1]
        )
        arcTo(
            b.radius, b.radius, 0.0,
            largeArcFlag = firstLarge, sweepFlag = sweepEven, end = points[3]
        )
        arcTo(
            radius, radius, 0.0,
            largeArcFlag = false, sweepFlag = true, end = points[2]
        )
        arcTo(
            a.radius, a.radius, 0.0,
            largeArcFlag = secondLarge, sweepFlag = sweepEven, end = points[0]
        )
        close()
    }
}

/**
 * Bends a list of [ShapeContour] with a [knife]
 */
fun List<ShapeContour>.bend(knife: ShapeContour) = this.map { contour ->
    val points = contour.equidistantPositions(contour.length.toInt())
        .toMutableList()

    if (points.isEmpty()) {
        return@map contour
    }

    // TODO: remove hardcoded 50. Should be an argument.
    // Or make it based on distance as I tried earlier.
    // So effect strength increases with distance to cut tip and
    // decreases with distance to cut-line.
    val pointCount = min(points.size, 50)
    if (knife.on(points.first(), 1.0) != null) {
        for (i in 0 until pointCount) {
            val origin = points[i].copy()
            val f = Random.simplex(points[i] * 0.03) * 200.0
//            val f = j.toDouble().map(i * 1.0, 0.0, 0.0, 1.0)
//                    .pow(2.0) * 10.0
            //val f = 1.0 / (d * 0.2)
            val nearest = knife.nearest(points[i])
            val d = nearest.position.distanceTo(points[i])
            //val n = nearest.contourT
            for (j in i downTo 0) {
                points[j] = points[j].rotate(f / (d + 1), origin)
            }
        }
    }
    if (knife.on(points.last(), 1.0) != null) {
        for (i in points.size - pointCount until points.size - 1) {
            val origin = points[i].copy()
            val f = -Random.simplex(points[i] * 0.03) * 200.0
//            val f = j.toDouble().map(i2 * 1.0, points.size - 1.0, 0.0, 1.0)
//                    .pow(2.0) * 10.0
            //val f = -1.0 / (d * 0.2)
            val nearest = knife.nearest(points[i])
            val d = nearest.position.distanceTo(points[i])
            //val n = nearest.contourT
            for (j in i until points.size) {
                points[j] = points[j].rotate(f / (d + 1), origin)
            }
        }
    }

    ShapeContour.fromPoints(points, false)
}

/**
 * Find all [ShapeContour] percentages passing through a [Vector2]
 * In case of an 8 shape, it could be 4.
 */
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

// TODO: idea: what if we consider self intersections as intersection
// in which the normal differs? Also, what happens when we search for
// intersections between two identical contours?
fun ShapeContour.selfIntersections(): List<Double> {
    val result = mutableListOf<Double>()
    val intersections = mutableListOf<Vector2>()
    this.segments.forEach {
        val p = this.intersections(it)
        if (p.isNotEmpty()) {
            intersections.addAll(p.map { intersection -> intersection.position })
        }
    }
    intersections.dedupe(1.2).forEach { p ->
        // the .on() function returns only 1 point, but with self
        // intersections we may get multiple points
        val points = this.onAll(p, 1.0)
        if (points.isNotEmpty()) {
            // use random to avoid always taking the first or the second
            // used for lines that are above or below
            result.add(points.random())
        }
    }
    return result.sorted()
}

// Linear shorten. Reduces a contours length by a distance to
// the ends. The percentage differs greatly if you compare a
// straight line to a line that ends in a spiral. In the second case
// the percent location would be much higher to reach the `d` distance.
// TODO: This can now be achieved simpler by just deleting everything that
// is inside a circle of radius `d`. How to delete? ClipMode.REVERSE_DIFFERENCE?
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


@Suppress("unused")
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
                result.add(
                    sub(
                        intPcs[index],
                        intPcs[index + 1]
                    ).shorten(margin)
                )
            } else {
                // Last one crosses the curve starting point
                // LAST .. END + START .. FIRST
                result.add(
                    (sub(intPcs[index], 1.0) +
                            sub(0.0, intPcs[0])).shorten(margin)
                )
            }
        }
        return result
    } else {
        return listOf(this)
    }
}

/**
 * Takes a curve and deletes the parts of it that are crossing
 * other curves. In the result it looks like one of the intersecting lines
 * is above and the other below.
 */
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
                if (intersections.isNotEmpty()) {
                    // FIXME: this method is broken since I switched from
                    // my own .intersections() to the new one from OPENRNDR
                    println("$aIndex to $bIndex -> ${intersections.size} elems")
                    val parts = listOf(
                        a.split(b).toMutableList(),
                        b.split(a).toMutableList()
                    )
                    removeAll(listOf(a, b))
                    intersections.forEach { intersection ->
                        val part = parts.random(Random.rnd)
                        part.forEachIndexed { i, c ->
                            val cutLen = margin / c.length
                            c.onAll(intersection.position, 1.0).forEach {
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

/**
 * Smooths out a [ShapeContour] by making sure all curve control points
 * are symmetric (so cp0-point-cp1 form a straight line).
 * [roundness] is a function that takes an index and returns a pair of
 * [Double] specifying how far away the previous and next control points are.
 * [symmetrize] returns a pair containing the new [ShapeContour] and also
 * a list of [LineSegment] in case one wants to draw the tangent lines.
 */
fun ShapeContour.symmetrize(
    roundness: (Int) -> Pair<Double, Double> = { Pair(0.333, 0.333) }
):
        Pair<ShapeContour, List<LineSegment>> {
    val visibleTangents = mutableListOf<LineSegment>()
    val tangents = segments.mapIndexed { i, curr ->
        val next = segments[(i - 1 + segments.size) % segments.size]
        (curr.direction()).mix(next.direction(), 0.5).normalized
    }
    val newSegments = segments.mapIndexed { i, currSegment ->
        val sz = segments.size
        val len = currSegment.length
        val iNext = (i + 1 + sz) % sz
        val c0 = currSegment.start +
                tangents[i] * len * roundness(i).first
        val c1 = currSegment.end -
                tangents[iNext] * len * roundness((i + 1) % sz).second
        visibleTangents.add(LineSegment(currSegment.start, c0))
        visibleTangents.add(LineSegment(c1, currSegment.end))
        visibleTangents.add(
            LineSegment(
                currSegment.start,
                currSegment.end
            )
        )
        Segment(currSegment.start, c0, c1, currSegment.end)
    }
    return Pair(ShapeContour(newSegments, closed), visibleTangents)
}

/**
 * Similar to [ShapeContour.symmetrize] but without returning tangent lines.
 */
fun ShapeContour.symmetrizeSimple(
    roundness: (Int) -> Pair<Double, Double> = { Pair(0.333, 0.333) }
):
        ShapeContour {
    val tangents = segments.mapIndexed { i, curr ->
        val next = segments[(i - 1 + segments.size) % segments.size]
        (curr.direction()).mix(next.direction(), 0.5).normalized
    }
    val newSegments = segments.mapIndexed { i, currSegment ->
        val sz = segments.size
        val len = currSegment.length
        val iNext = (i + 1 + sz) % sz
        val c0 = currSegment.start +
                tangents[i] * len * roundness(i).first
        val c1 = currSegment.end -
                tangents[iNext] * len * roundness((i + 1) % sz).second
        Segment(currSegment.start, c0, c1, currSegment.end)
    }
    return ShapeContour(newSegments, closed)
}


/**
 * Creates a deformed circle centered at [pos] and with radius [radius]
 */
fun circleish(pos: Vector2, radius: Double, angularOffset: Double = 0.0) =
    CatmullRomChain2(List(5) {
        it * 72 + angularOffset + Random.double0(50.0)
    }.map {
        Polar(it, radius).cartesian + pos
    }, 0.5, true).toContour()

/**
 * Creates a deformed circle centered at [center] and with radius [radius]
 */
fun circleish2(
    center: Vector2,
    radius: Double,
    pointCount: Int = 100,
    orientation: Double = 0.0,
    noiseScale: Double = 0.1,
    noiseFreq: Double = 1.0
): ShapeContour {
    return ShapeContour.fromPoints(
        List(pointCount) { i ->
            val angle = i / pointCount.toDouble()
            val cycle = sin(angle * PI * 2 + orientation) * noiseFreq
            val maxOffset = radius * noiseScale
            val offset =
                Random.simplex(cycle.pow(5.0), center.x, center.y) * maxOffset
            center + Polar(angle * 360, radius + offset).cartesian
        }, true
    )
}
