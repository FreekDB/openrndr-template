package geometry

import org.openrndr.math.Vector2

fun Vector2.distance(other: Vector2): Double {
    return (this - other).length
}
