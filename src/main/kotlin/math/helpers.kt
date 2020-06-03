package math

import org.openrndr.extra.noise.Random
import org.openrndr.math.map
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow

fun angleDiff(a: Double, b: Double): Double {
    val dist: Double = (a - b + 360) % 360
    return if (dist > 180) dist - 360 else dist
}

// TODO: this is weird. noise of min, max???
// Not the original function. Why?
fun doubleExponentialSigmoid(min: Double, max: Double): Double {
    val x = Random.simplex(min, max) * 0.5 + 0.5
    val a = 0.15

    return if (x <= 0.5) {
        ((2.0 * x).pow(1.0 / a)) / 2.0;
    } else {
        1.0 - ((2.0 * (1.0 - x)).pow(1.0 / a)) / 2.0;
    }
}

/**
 * Cosine envelope that goes up and down.
 * Returns 0.0 at both edges, 1.0 in the middle
 */
fun cosEnv(x: Double, start: Double = 0.0, end: Double = 1.0): Double {
    val xNorm = x.map(start, end, 0.0, PI * 2)
    return 0.5 - 0.5 * cos(xNorm)
}
