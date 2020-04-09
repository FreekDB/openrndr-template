package geometry

import org.openrndr.extra.noise.Random
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import kotlin.math.*

fun fromIrregularLine(p0: Vector2, p1: Vector2, pc: Double, time: Double = 0.0): Vector2 {
    val offset = cos(pc * PI * 2 - PI) * 0.5 + 0.5
    val distance = (p0 - p1).length * 0.15
    val point = mix(p0, p1, pc)
    return point + Vector2(
        distance * Random.simplex(point.x * 0.01 - time, point.y * 0.01) * offset,
        distance * Random.simplex(point.y * 0.01 + time, point.x * 0.01) * offset
    )
}

fun angleToSquare(angle: Double, radius: Double): Vector2 {
    val square = min(
        1 / abs(cos(Math.toRadians(angle))),
        1 / abs(sin(Math.toRadians(angle)))
    );
    return Polar(angle, radius * square).cartesian
}