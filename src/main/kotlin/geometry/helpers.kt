package geometry

import org.openrndr.math.Vector2
import org.openrndr.shape.Circle
import kotlin.math.abs

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

    for(point in sorted) {
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
 * Separate Vector2 points
 */
fun List<Circle>.separated(separation: Double): List<Circle> {
    return this.map { me ->
        var sum = Vector2.ZERO
        var count = 0
        this.forEach { other ->
            val d = me.center.distanceTo(other.center) - me.radius - other.radius
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
