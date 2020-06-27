package apps.simpleTests

import org.openrndr.application
import org.openrndr.draw.colorBuffer
import org.openrndr.extra.gui.GUI

fun main() = application {
    configure {
        height = 1500
    }
    program {
        val a = colorBuffer(16, 16)
        val b = colorBuffer(16, 16)

        extend(GUI())
        extend {

            if (keyboard.pressedKeys.contains("a")) {
                a.copyTo(b)
            }
            drawer.circle(200.0, 200.0, 200.0)
        }
    }
}
