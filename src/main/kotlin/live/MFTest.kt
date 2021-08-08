package live

import aBeLibs.extensions.*
import aBeLibs.extensions.MidiFighter.Color.BLUE
import aBeLibs.math.Interpolator
import org.openrndr.applicationSynchronous
import org.openrndr.color.rgb
import org.openrndr.extra.noise.random
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import kotlin.math.max

/**
 * Here I'm considering if it makes sense to use annotations
 * in variables to link them to midi knobs and buttons.
T*
 * Q: Could I use midi-learn instead of hardcoding the cc-number? For that I would need a list of variables
 *    I can control, then keys to highlight one of those variables, then turn a knob or press a button
 */

fun main() = applicationSynchronous {
    program {
        val midi = @Description("world") object {
            var bri = 1.0

            @MFSigned("Angular rotation", ccnum = 12, style = 30)
            val rotation = Interpolator(0.0)

            @MFDouble("x", ccnum = 13)
            val x = Interpolator(0.5, 0.008, 0.001)

            @MFDouble("y", ccnum = 14)
            val y = Interpolator(0.1)

            // This for a second midi controller (Faderfox)
            @FFAction("Faderfox click", ch = 12, ccnum = 0)
            @Suppress("unused")
            fun doSomething() {
                println("something")
            }

            // Setting the name on the device is not yet possible
            @FFDouble("SHKE", ch = 4, ccnum = 0)
            var shake = 0.0

            @MFAction("Centered flash", ccnum = 15, color = BLUE)
            @Suppress("unused")
            fun flash() {
                bri = 1.0
            }
        }
        val mf = MidiFighter()
        val ff = FaderFox()
        extend(mf) {
            add(midi)
        }
        extend(ff) {
            add(midi)
        }

        extend {
            drawer.run {
                translate(
                    midi.x * width,
                    midi.y * height +
                            random(0.0, midi.shake * 20)
                )
                rotate(midi.rotation * 180.0)
                fill = rgb(midi.bri)
                rectangle(Rectangle.fromCenter(Vector2.ZERO, 200.0, 200.0))
            }
            midi.bri = max(0.0, midi.bri - 0.01)
        }

        keyboard.keyDown.listen {
            when (it.key) {
                55 -> mf.setPage(0)
                56 -> mf.setPage(1)
                57 -> mf.setPage(2)
                48 -> mf.setPage(3)
                else -> println(it.key)
            }
        }
    }
}
