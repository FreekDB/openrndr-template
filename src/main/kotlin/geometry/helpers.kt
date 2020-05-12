package geometry

import org.openrndr.math.Vector2

/**
 * Calculate the convex hull of a list of 2D points
 * https://rosettacode.org/wiki/Convex_hull#Kotlin
 */
// counter-clockwise turn
fun cw(a: Vector2, b: Vector2, c: Vector2) =
    ((b.x - a.x) * (c.y - a.y)) < ((b.y - a.y) * (c.x - a.x))

@ExperimentalStdlibApi
fun convexHull(p: List<Vector2>): List<Vector2> {
    if (p.size <= 3) return p
    val sorted = p.sortedBy { it.x }
    val result = mutableListOf<Vector2>()

    sorted.forEach {
        result.run {
            while (size >= 2 && cw(this[size - 2], last(), it)) {
                removeLast()
            }
            add(it)
        }
    }

    val t = result.size + 1
    for (i in sorted.size - 2 downTo 0) {
        val it = sorted[i]
        result.run {
            while (size >= t && cw(this[size - 2], last(), it)) {
                removeLast()
            }
            add(it)
        }
    }
    result.removeLast()
    return result
}
