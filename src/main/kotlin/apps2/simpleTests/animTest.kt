package apps2.simpleTests

import org.openrndr.animatable.Animatable
import org.openrndr.animatable.easing.Easing
import org.openrndr.application
import org.openrndr.color.ColorHSVa
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.noise.Random

fun main() = application {
    program {
        class Pos() : Animatable() {
            var x: Double = 50.0
            var y: Double = 50.0
        }
        var bg = ColorRGBa.BLACK
        val pos = Pos()

        extend {
            pos.updateAnimation()
            drawer.clear(bg)
            drawer.circle(pos.x, pos.y, 50.0)
        }

        keyboard.keyDown.listen {
            pos.x = 200.0
            pos.y = 200.0
            pos.animate("x", Random.double0(width * 1.0), 2000, Easing.QuadInOut)
            pos.complete()
            pos.animate("y", Random.double0(height * 1.0), 1200, Easing.CubicInOut)
            pos.animate("x", Random.double0(width * 1.0), 2000, Easing.CubicInOut)
            pos.complete {
                bg = ColorHSVa(Random.double0(360.0), 0.5, 0.5).toRGBa()
            }
        }
    }
}
