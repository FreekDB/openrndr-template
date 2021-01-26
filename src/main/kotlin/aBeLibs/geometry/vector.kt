package aBeLibs.geometry

import org.openrndr.extra.noise.Random
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.panel.elements.round

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
fun Polar.rotated(theta: Double): Polar {
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

/**
 * .round() but applied to Vector2
 */
fun Vector2.round(decimals: Int): Vector2 {
    return Vector2(x.round(decimals), y.round(decimals))
}
