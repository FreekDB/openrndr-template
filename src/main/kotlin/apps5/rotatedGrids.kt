package apps5

import org.openrndr.UnfocusBehaviour
import org.openrndr.WindowMultisample
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolated
import org.openrndr.extra.noise.random
import org.openrndr.extra.shapes.grid
import org.openrndr.extras.color.presets.BURLY_WOOD
import org.openrndr.extras.color.presets.LIGHT_CORAL
import org.openrndr.extras.color.presets.LIGHT_SALMON
import kotlin.random.Random

fun main() = application {
    configure {
        width = 900
        height = 900
        multisample = WindowMultisample.SampleCount(8)
        title = "grids"
        unfocusBehaviour = UnfocusBehaviour.THROTTLE
    }
    program {
        fun n() = Random.nextInt(5, 25)
        fun g() = random(5.0, 30.0)
        extend {
            drawer.translate(drawer.bounds.center)
            drawer.stroke = null
            drawer.isolated {
                rotate(5.0)
                translate(-bounds.center)
                fill = ColorRGBa.BURLY_WOOD
                rectangles(
                    bounds.grid(n(), n(), 50.0, 50.0, g(), g()).flatten()
                )
            }
            drawer.isolated {
                rotate(1.0)
                translate(-bounds.center)
                fill = ColorRGBa.LIGHT_CORAL
                rectangles(
                    bounds.grid(n(), n(), 50.0, 50.0, g(), g()).flatten()
                )
            }
            drawer.isolated {
                rotate(-3.0)
                translate(-bounds.center)
                fill = ColorRGBa.LIGHT_SALMON
                rectangles(
                    bounds.grid(n(), n(), 50.0, 50.0, g(), g()).flatten()
                )
            }
        }
    }
}