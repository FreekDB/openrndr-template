package aBeLibs.geometry

import org.openrndr.extra.noise.uniform
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.shape.*
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Calculate the convex hull of a list of 2D points
 * https://rosettacode.org/wiki/Convex_hull#Kotlin
 */
@ExperimentalStdlibApi
fun convexHull(p: List<Vector2>): List<Vector2> {
    if (p.size <= 3) return p
    val sorted = p.sortedBy { it.x }
    val result = mutableListOf<Vector2>()

    fun clockWise(a: Vector2, b: Vector2, c: Vector2) =
        ((b.x - a.x) * (c.y - a.y)) < ((b.y - a.y) * (c.x - a.x))

    fun MutableList<Vector2>.tryAdd(point: Vector2, cutIndex: Int) {
        while (size >= cutIndex && clockWise(this[size - 2], last(), point)) {
            removeLast()
        }
        add(point)
    }

    for (point in sorted) {
        result.tryAdd(point, 2)
    }
    val cutIndex = result.size + 1
    for (i in sorted.size - 2 downTo 0) {
        result.tryAdd(sorted[i], cutIndex)
    }
    result.removeLast()
    return result
}

/**
 * Separate Vector2 points
 */
fun List<Vector2>.separate(separation: Double): List<Vector2> {
    return this.map { me ->
        var sum = Vector2.ZERO
        var count = 0
        this.forEach { other ->
            val d = me.distanceTo(other)
            if (d < separation) {
                var force = (me - other).normalized
                force /= abs(d)
                sum += force
                count++;
            }
        }
        if (count > 0) {
            sum = sum.normalized
        }
        me + sum
    }
}

/**
 * Separate Circles in a List
 */
fun List<Circle>.separated(
    separation: Double,
    container: ShapeContour? = null
): List<Circle> {
    return this.map { me ->
        var sum = Vector2.ZERO
        var count = 0
        this.forEach { other ->
            val d =
                me.center.distanceTo(other.center) - me.radius - other.radius
            if (d < separation) {
                var force = (me.center - other.center).normalized
                force /= abs(d)
                sum += force
                count++;
            }
        }
        if (count > 0) {
            sum = sum.normalized
        }
        Circle(me.center + sum, me.radius)
    }
}

/**
 * SDF square
 */
fun angleToSquare(angle: Double, radius: Double): Vector2 {
    val square = min(
        1 / abs(cos(Math.toRadians(angle))),
        1 / abs(sin(Math.toRadians(angle)))
    );
    return Polar(angle, radius * square).cartesian
}

/**
 * Returns a random point inside a rectangle
 */
fun Rectangle.randomPoint(): Vector2 {
    return Vector2.uniform(this.corner, this.corner + this.dimensions)
}

/**
 * For a Composition, filter out bezier segments contained in longer bezier segments.
 * The goal is to avoid drawing lines multiple times with a plotter.
 */
fun Composition.dedupe(): Composition {
    val segments = this.findShapes()
        .map { it.shape.contours.map { it.segments }.flatten() }.flatten()
    val deduped = segments.filter { curr ->
        segments.none { other -> other.contains(curr, 1.0) }
    }.map { it.contour }
    return drawComposition {
        contours(deduped)
    }
}

/**
 * Simple test to see if a segment contains a different Segment.
 * Compares start, end and two points at 1/3 and 2/3.
 * Returns false when comparing a Segment to itself.
 */
fun Segment.contains(other: Segment, error: Double = 0.5): Boolean =
    this !== other &&
            this.on(other.start, error) != null &&
            this.on(other.end, error) != null &&
            this.on(other.position(1.0 / 3), error) != null &&
            this.on(other.position(2.0 / 3), error) != null

