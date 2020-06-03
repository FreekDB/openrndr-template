package p5

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.math.Vector2
import org.openrndr.math.mix
import org.openrndr.shape.Circle
import org.openrndr.shape.LineSegment
import kotlin.math.sqrt

// see https://stackoverflow.com/questions/1073336/circle-line-segment-collision-detection-algorithm

fun main() = application {
    program {
        val line = LineSegment(100.0, 100.0, 400.0, 400.0)
        var cir = Circle(Vector2.ZERO, 90.0)

        extend {
            drawer.clear(ColorRGBa.WHITE)
            drawer.fill = ColorRGBa.fromHex("FFAA00").opacify(0.4)
            drawer.stroke = ColorRGBa.BLACK.opacify(0.4)
            drawer.strokeWeight = 2.0

            cir = cir.movedTo(mouse.position)
            drawer.lineSegment(line)
            drawer.circle(cir)
            drawer.circles(line.intersections(cir), 10.0)
        }
    }
}

private fun LineSegment.intersections(cir: Circle): List<Vector2> {
    val d = end - start
    val f = start - cir.center

    val result = mutableListOf<Vector2>()

    val a = d.dot(d)
    val b = 2 * f.dot(d)
    val c = f.dot(f) - cir.radius * cir.radius

    var discriminant = b * b - 4 * a * c
    if (discriminant < 0) {
        return result
    }

    discriminant = sqrt(discriminant)

    val t1 = (-b - discriminant) / (2 * a)
    val t2 = (-b + discriminant) / (2 * a)

    if (t1 in 0.0..1.0) {
        result.add(mix(start, end, t1))
    }

    if (t2 in 0.0..1.0) {
        result.add(mix(start, end, t2))
    }

    return result
}
