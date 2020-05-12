package geometry

import org.openrndr.extra.noise.Random
import org.openrndr.math.Polar
import org.openrndr.math.Vector2

//fun mix(a: Vector2, b: Vector2, x: Double): Vector2 {
//    return a * (1 - x) + b * x
//}
//
//fun Vector2.lerp(other: Vector2, x: Double): Vector2 {
//    return this * (1 - x) + other * x
//}

/**
 *
 */
fun Polar.rotate(theta: Double): Polar {
    return Polar(this.theta + theta, radius)
}

/**
 * Displace a vector using Simplex noise, using it's
 * own pasition as input
 */
fun Vector2.noised(amt: Double = 1.0): Vector2 {
    return this + Vector2(
        Random.simplex(this),
        Random.simplex(this.yx + 3.17)
    ) * amt
}
