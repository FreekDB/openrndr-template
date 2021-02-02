package aBeLibs.math

import org.openrndr.extra.noise.Random
import org.openrndr.math.Vector2
import org.openrndr.math.clamp
import org.openrndr.math.map
import org.openrndr.math.mod
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow

const val TAU = PI * 2.0

fun angleDiff(degrees0: Double, degrees1: Double): Double {
    val dist: Double = (degrees0 - degrees1 + 360) % 360
    return if (dist > 180) dist - 360 else dist
}

/**
 * Angle formed by 3 points <
 */
fun angle(center: Vector2, p1: Vector2, p2: Vector2) =
    atan2(p1.y - center.y, p1.x - center.x) - atan2(p2.y - center.y, p2.x - center.x)

/**
 * Is angle reflex? (PI, TAU)°
 */
fun isAngleReflex(angle: Double) = mod(angle + 1.5 * TAU, TAU) > PI

// TODO: this is weird. noise of min, max???
// Not the original function. Why?
fun doubleExponentialSigmoid(min: Double, max: Double): Double {
    val x = Random.simplex(min, max) * 0.5 + 0.5
    val a = 0.15

    return if (x <= 0.5) {
        ((2.0 * x).pow(1.0 / a)) / 2.0
    } else {
        1.0 - ((2.0 * (1.0 - x)).pow(1.0 / a)) / 2.0
    }
}

/**
 * Cosine envelope that goes up and down.
 * Returns 0.0 at both edges, 1.0 in the middle
 */
fun cosEnv(x: Double, start: Double = 0.0, end: Double = 1.0, clamped: Boolean = false): Double {
    if (clamped && (x < start || x > end)) {
        return 0.0
    }
    val xNorm = x.map(start, end, 0.0, TAU)
    return 0.5 - 0.5 * cos(xNorm)
}

/**
 * Shift normalized values below a threshold up or down. Example
 * compress(val, 0.3, 0.9); compresses
 * [0.0..0.3..1.0] => [0.0..0.9..1.0]
 *
 * @param x
 * @param from
 * @param to
 * @return
 */
@Suppress("unused")
fun compress(x: Double, from: Double, to: Double): Double {
    return if (x < from) {
        map(0.0, from, 0.0, to, x)
    } else {
        map(from, 1.0, to, 1.0, x)
    }
}

/**
 * Opposite of norm(val, min, max).
 * denormalize(0.5, 10, 20) returns 15.
 *
 * @param normValue
 * @param min
 * @param max
 * @return
 */
@Suppress("unused")
fun denormalize(normValue: Double, min: Double, max: Double): Double {
    return min + normValue * (max - min)
}

/**
 * Similar to PApplet.norm() but ensure 0..1 range
 *
 * @param value any input value
 * @param start the lower bound
 * @param stop  the higher bound
 * @return a normalized value, clipped to [0, 1] if value was outside
 * the [start, stop] range
 */
@Suppress("unused")
fun normClipped(value: Double, start: Double, stop: Double) = clamp((value - start) / (stop - start))

/**
 * Constrain to 0 .. 1 range
 *
 * @param value
 * @return a value between 0.0f and 1.0f
 */
fun clamp(value: Double): Double = clamp(value, 0.0, 1.0)

fun Double.map(afterLeft: Double, afterRight: Double) =
    this.map(0.0, 1.0, afterLeft, afterRight)

/** maps a vector2 from one Rectangle to another Rectangle */
// Exists in Rectangle, but was not finding it for some reason.