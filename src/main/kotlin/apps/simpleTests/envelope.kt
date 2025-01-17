package apps.simpleTests

import aBeLibs.anim.Envelope
import org.openrndr.application
import org.openrndr.extra.noise.Random

/**
 * id: 48e86e5a-c14d-4034-9869-48e83944dbe2
 * description: New sketch
 * tags: #new
 */

fun main() = application {
    program {
        val x = Envelope(100.0)
        val y = Envelope(100.0)
        val size = Envelope(40.0, 400.0, 40.0, 300.0, 60.0, 200.0, 60.0, 300.0, 40.0, repetitions = -1)

        extend {
            Envelope.tick(x, y, size)
            drawer.circle(x * 1.0, y * 1.0, size.toDouble())
        }

        mouse.buttonUp.listen {
            x.append(it.position.x * 1.0, Random.double(500.0, 1500.0))
            y.append(it.position.y * 1.0, Random.double(500.0, 1500.0))
        }

        keyboard.keyDown.listen {
            when (it.name) {
                "b" -> {
                    x.boomerang(100.0, width * 0.5, 2000.0)
                    y.boomerang(200.0, height * 0.5, 1900.0)
                }
            }
        }
    }
}
