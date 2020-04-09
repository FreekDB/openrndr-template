package geometry

import org.openrndr.math.Vector2

fun mix(a: Vector2, b: Vector2, x: Double): Vector2 {
    return a * (1.0 - x) + b * x
}