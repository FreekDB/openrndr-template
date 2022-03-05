package latest

import aBeLibs.geometry.dedupe
import org.openrndr.KEY_SPACEBAR
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.dialogs.saveFileDialog
import org.openrndr.shape.ClipMode
import org.openrndr.shape.drawComposition
import org.openrndr.svg.saveToFile

/**
 * id: af3ba4f3-d37c-4b56-a8ca-7067c6245437
 * description: An example calling svg.dedupe() to
 * remove duplicate segments
 * after doing boolean operations on closed shapes.
 * tags: #new
 */

fun main() = application {
    program {
        val svg = drawComposition {
            fill = null
            circle(width / 2.0 - 100.0, height / 2.0, 100.0)
            circle(width / 2.0 + 100.0, height / 2.0, 100.0)
            clipMode = ClipMode.REVERSE_DIFFERENCE
            circle(width / 2.0, height / 2.0, 100.0)
        }
        extend {
            drawer.clear(ColorRGBa.WHITE)
            drawer.composition(svg)
        }
        keyboard.keyDown.listen {
            when (it.key) {
                KEY_SPACEBAR -> saveFileDialog(supportedExtensions = listOf("svg")) { file ->
                    svg.dedupe().saveToFile(file)
                }
            }
        }
    }
}