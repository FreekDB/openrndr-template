package math

fun angleDiff(a: Double, b: Double): Double {
    val dist: Double = (a - b + 360) % 360
    return if (dist > 180) dist - 360 else dist
}
