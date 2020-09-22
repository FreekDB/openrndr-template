package apps

import kotlinx.coroutines.yield
import org.openrndr.application
import org.openrndr.launch

/**
 * Here I'm considering if it makes sense to use annotations
 * in variables to link them to midi knobs and buttons.
 *
 * Q: Midi to object seems clear.
 * Q: How to use annotations? Look at the UI classes
 * Q: What's the goal of annotations in this file? They set the cc-number, detent, style
 * Q: Could I use midi-learn instead of hardcoding the cc-number? For that I would need a list of variables
 *    I can control, then keys to highlight one of those variables, then turn a knob or press a button
 */

/*
fun main() = application {
    program {
        dispatcher
        val world = @Description("world") object {
            @MidiSigned("Angular rotation", cc = 22, detent = true, style = 15)
            val rotation = -0.5

            // Does it provide some kind of benefit to split the
            // functionality into MidiDouble and Interpolated?
            // It could be a single instance of an object like
            // `MidiKnob`.
            @MidiDouble("Speed", cc = 23)
            val speed = Interpolated(0.1, speed = 0.3)

            @MidiAction("Centered flash", cc = 10, color = 23, style = 4)
            fun flash() {
            }
        }
        val mf = MidiFighter()

        extend(mf) {
            add(world)
        }
        extend {
            mf.setPage(2)
        }
    }
}
*/