package apps2.simpleTests

import org.openrndr.animatable.Animatable
import org.openrndr.animatable.easing.Easing
import org.openrndr.applicationSynchronous
import org.openrndr.color.ColorHSVa
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.noise.Random

fun main() = applicationSynchronous {
    program {
        var bg = ColorRGBa.BLACK
        val pos = object : Animatable() {
            var x = 50.0
            var y = 50.0
        }

        extend {
            pos.updateAnimation()
            drawer.clear(bg)
            drawer.circle(pos.x, pos.y, 50.0)
        }

        keyboard.keyDown.listen {
            pos.x = 200.0
            pos.y = 200.0
            pos.apply {
                ::x.animate(
                    Random.double0(width * 1.0),
                    2000,
                    Easing.QuadInOut)
                ::x.complete()
                ::y.animate(
                    Random.double0(height * 1.0),
                    1200,
                    Easing.CubicInOut
                )
                ::x.animate(
                    Random.double0(width * 1.0),
                    2000,
                    Easing.CubicInOut
                ).completed.listen {
                    bg = ColorHSVa(Random.double0(360.0), 0.5, 0.5).toRGBa()
                }
            }
        }
    }
}
