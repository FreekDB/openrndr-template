package math

import org.openrndr.extra.noise.Random
import kotlin.math.pow

fun angleDiff(a: Double, b: Double): Double {
    val dist: Double = (a - b + 360) % 360
    return if (dist > 180) dist - 360 else dist
}

fun doubleExponentialSigmoid(min: Double, max: Double): Double {
    val x = Random.simplex(min, max) * 0.5 + 0.5
    val a = 0.15

    return if (x <= 0.5) {
        ((2.0 * x).pow(1.0 / a)) / 2.0;
    } else {
        1.0 - ((2.0 * (1.0 - x)).pow(1.0 / a)) / 2.0;
    }
}