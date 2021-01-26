package apps2

import aBeLibs.extensions.Description
import aBeLibs.extensions.MFAction
import aBeLibs.extensions.MidiFighter
import aBeLibs.extensions.MFSigned
import aBeLibs.extensions.MidiFighter.Color.BLUE
import org.openrndr.application
import org.openrndr.color.rgb
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import kotlin.math.max

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

fun main() = application {
    program {
        // 2020 08 16 - What is this dispatcher?? Maybe used also in the UI case?
        //dispatcher
        var bri = 1.0
        val conf = @Description("world") object {
            // from -1.0 to +1.0 ?
            @MFSigned("Angular rotation", ccnum = 12, style = 15)
            var rotation = -0.5

            // Does it provide some kind of benefit to split the
            // functionality into MidiDouble and Interpolated?
            // Did I mean MidiSigned instead of Interpolated?
            // It could be a single instance of an object like `MidiKnob`.
            // from 0.0 to 1.0?
            //@MidiDouble("Speed", ccnum = 23)
            //val speed = Interpolated(0.1, speed = 0.3)

            @MFAction("Centered flash", ccnum = 15, color = BLUE)
            fun flash() {
                bri = 1.0
            }
        }
        val mf = MidiFighter()

        // This implies there's an extension for the MidiFighter
        extend(mf) {
            add(conf)
        }
        extend {
            drawer.run {
                translate(bounds.center)
                rotate(360.0 * conf.rotation)
                fill = rgb(bri)
                rectangle(Rectangle.fromCenter(Vector2.ZERO, 200.0, 200.0))
            }
            bri = max(0.0, bri - 0.01)
        }
        keyboard.keyDown.listen {
            if (it.name == "2") {
                mf.setPage(2)
            }
        }
    }
}
