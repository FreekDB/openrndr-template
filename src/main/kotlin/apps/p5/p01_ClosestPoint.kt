package apps.p5

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.noise.Random

/**
 * id: bb1e6afe-a92c-4530-a7d5-a5f1be4a9831
 * description: New sketch
 * tags: #new
 */

fun main() = application {
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
