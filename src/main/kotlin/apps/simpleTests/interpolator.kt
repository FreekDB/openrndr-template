package apps.simpleTests

import aBeLibs.math.Interpolator
import org.openrndr.application

/**
 * id: 12e8c4e4-d938-4124-a1bd-82d20116fa47
 * description: Click to set target any time. Interpolator has inertia.
 * tags: #new
 */

fun main() = application {
    program {
        val x = Interpolator(0.0, 0.0001, 0.01, 0.25)
        val y = Interpolator(0.0, 0.0001, 0.01, 0.25)

        extend {
            drawer.circle(
                x.getNext() * width,
                y.getNext() * height,
                50.0
            )
        }

        mouse.buttonDown.listen {
            val target = it.position / drawer.bounds.dimensions
            x.targetValue = target.x
            y.targetValue = target.y
        }
    }
}
