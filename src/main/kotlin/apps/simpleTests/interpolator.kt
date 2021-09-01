package apps.simpleTests

import aBeLibs.math.Interpolator
import org.openrndr.application
import org.openrndr.math.Vector2

// Click to set target any time. Interpolator has inertia.

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
