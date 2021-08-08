package apps.simpleTests

import org.openrndr.applicationSynchronous
import org.openrndr.draw.colorBuffer
import org.openrndr.draw.renderTarget
import org.openrndr.extra.fx.color.SetBackground
import org.openrndr.extra.gui.GUI

fun main() = applicationSynchronous {
    configure {
        height = 1000
    }
    program {
        val gui = GUI()
        val bw = renderTarget(16, 16) {
            colorBuffer()
        }
        val withFX = colorBuffer(16, 16)
        val fx = SetBackground()

        extend(gui) {
            add(fx)
        }
        extend {
            if (keyboard.pressedKeys.contains("a")) {
                fx.apply(bw.colorBuffer(0), withFX)
            }
        }
    }
}
