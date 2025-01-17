package apps.simpleTests

import aBeLibs.anim.Envelope2
import org.openrndr.animatable.easing.Easing
import org.openrndr.application
import org.openrndr.extra.noise.Random

/**
 * id: 9059fe85-9c74-4eb7-950c-9c615289b054
 * description: New sketch
 * tags: #new
 */

fun main() = application {
    program {
        val x = Envelope2(100.0)
        val y = Envelope2(listOf(100.0, 200.0), repetitions = 4)
        val size = Envelope2(
            listOf(40.0, 40.0, 60.0, 60.0),
            listOf(800, 400, 200, 100),
            listOf(Easing.CubicInOut), repetitions = -1
        )

        extend {
            Envelope2.tick(x, y, size)
            drawer.circle(x * 1.0, y * 1.0, size.toDouble())
        }

        mouse.buttonUp.listen {
            x.append(it.position.x * 1.0, Random.int(500, 1500))
            y.append(it.position.y * 1.0, Random.int(500, 1500))
        }

        keyboard.keyDown.listen {
            when (it.name) {
                "b" -> {
                    x.boomerang(2000, width * 0.5, 2000)
                    y.addDelay(1000)
                    y.boomerang(2000, height * 0.5, 2000)
                }
                "a" -> {
                    x.animateTo(
                        Random.double0(width * 1.0),
                        easing = Random.pick(listOf(Easing.QuadIn, Easing.QuadOut))
                    )
                }
                "d" -> {
                    x.addDelay(4000)
                }
            }
        }
    }
}
