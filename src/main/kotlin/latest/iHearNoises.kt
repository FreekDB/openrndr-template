package latest

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.color.rgba
import org.openrndr.extra.noclear.NoClear
import org.openrndr.extra.noise.Random
import org.openrndr.math.Polar

/**
 * id: 65b787c5-1cec-4dc9-ac59-be02f22d8865
 * description: New sketch
 * tags: #new
 */

fun main() = application {
    configure {
        width = 800
        height = 800
    }
    program {
        val zoom = 0.03
        backgroundColor = ColorRGBa.WHITE
        extend(NoClear())
        extend {
            drawer.fill = rgba(0.0, 0.0, 0.0, 0.01)
            val positions = mutableListOf(Random.point(drawer.bounds))
            drawer.points(generateSequence(Random.point(drawer.bounds)) {
                it + Polar(
                    180 * if (it.x < width / 2)
                        Random.perlin(it.vector3(z = seconds) * zoom)
                    else
                        Random.cubic(it.vector3(z = seconds) * zoom)
                ).cartesian
            }.take(500).toList())
        }
    }
}
