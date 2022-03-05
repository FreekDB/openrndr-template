package latest

import org.openrndr.KEY_ENTER
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.dialogs.saveFileDialog
import org.openrndr.extensions.Screenshots
import org.openrndr.shape.Circle
import org.openrndr.shape.ClipMode
import org.openrndr.shape.drawComposition
import org.openrndr.svg.saveToFile

/**
 * id: 4a47cbb0-4b4e-4026-9864-18aab1593f58
 * description: New sketch
 * tags: #new
 */

fun main() = application {
    program {
        val circleShapes = List(20) {
            //val pos = drawer.bounds.center + Polar(Random.double0(360.0), 160.0).cartesian
            val pos = drawer.bounds.position(0.1, 0.1).mix(
                drawer.bounds.position(0.9, 0.9), it / 20.0
            )
            val r = 10.0 + (it % 4) * 5.0
            Circle(pos, r)
        }
        val svg = drawComposition {
            fill = null
            circle(drawer.bounds.center, 150.0)
            clipMode = ClipMode.REVERSE_DIFFERENCE
            circles(circleShapes)
        }

        extend(Screenshots())
        extend {
            drawer.clear(ColorRGBa.PINK)
            drawer.composition(svg)
            drawer.circles(circleShapes.map { it.center }, 5.0)
        }
        keyboard.keyDown.listen {
            when (it.key) {
                KEY_ENTER -> saveFileDialog(supportedExtensions = listOf("svg")) { file ->
                    svg.saveToFile(file)
                }
            }

        }
    }
}
