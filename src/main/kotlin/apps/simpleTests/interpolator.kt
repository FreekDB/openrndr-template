package apps.simpleTests

import math.Interpolator
import org.openrndr.application
import org.openrndr.math.Vector2

// Click to set target any time. Interpolator has inertia.

fun main() = application {
    program {
        val x = Interpolator(0.0, 0.0001, 0.01, 0.25)
        val y = Interpolator(0.0, 0.0001, 0.01, 0.25)

        var target = Vector2.ONE * 0.5

        extend {
            drawer.circle(
                x.getNextForTarget(target.x) * width,
                y.getNextForTarget(target.y) * height,
                50.0
            )
        }

        mouse.clicked.listen {
            target = it.position / drawer.bounds.dimensions
        }
    }
}
