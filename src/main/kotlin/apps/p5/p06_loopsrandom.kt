package apps.p5

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.noise.Random
import org.openrndr.math.Vector2
import org.openrndr.math.map

/**
 * id: 8928945c-3511-4ac2-ab8e-90dca144d8b0
 * description: New sketch
 * tags: #new
 */

fun main() = application {
    configure {
        width = 900
        height = 450
    }
    program {
        val colors = listOf(ColorRGBa.PINK, ColorRGBa.WHITE, ColorRGBa.WHITE, ColorRGBa.PINK.shade(0.5))
        extend {
            Random.resetState()
            drawer.clear(ColorRGBa.WHITE)
            for (x in 0..15) {
                for (y in 0..5) {
                    val pos = Vector2(
                        map(0.0, 15.0, 100.0, width - 100.0, x.toDouble()),
                        map(0.0, 5.0, 100.0, height - 100.0, y.toDouble())
                    )
                    drawer.stroke = ColorRGBa.PINK.opacify(0.4)
                    val amount = Random.simplex(pos.x * 0.004, pos.y * 0.003) + 0.5
                    drawer.fill = ColorRGBa.PINK.shade(amount)
                    drawer.circle(pos, 20.0)
                    if (Random.bool()) {
                        drawer.fill = Random.pick(colors)
                        drawer.circle(pos, 10.0)
                    }
                }
            }
        }
    }
}
