package apps.p5

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.noise.Random

fun main() = application {
    program {
        val points = List(30) { drawer.bounds.center + drawer.bounds.center * Random.Vector2() }
        extend {
            drawer.background(ColorRGBa.WHITE)
            val closest = points.minBy { (mouse.position - it).squaredLength }
            points.forEach {
                drawer.fill = if (closest == it) ColorRGBa.RED else ColorRGBa.WHITE
                drawer.circle(it, 20.0)
            }
        }
    }
}
