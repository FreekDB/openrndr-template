package apps.p5

import org.openrndr.applicationSynchronous
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.noise.Random

fun main() = applicationSynchronous {
    program {
        val points = List(30) { drawer.bounds.center + drawer.bounds.center * Random.vector2() }
        extend {
            drawer.clear(ColorRGBa.WHITE)
            val closest =
                points.minByOrNull { (mouse.position - it).squaredLength }
            points.forEach {
                drawer.fill = if (closest == it) ColorRGBa.RED else ColorRGBa.WHITE
                drawer.circle(it, 20.0)
            }
        }
    }
}
