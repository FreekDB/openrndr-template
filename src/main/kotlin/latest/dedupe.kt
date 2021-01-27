package apps2

import aBeLibs.geometry.dedupe
import org.openrndr.KEY_SPACEBAR
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.dialogs.saveFileDialog
import org.openrndr.shape.ClipMode
import org.openrndr.shape.Segment
import org.openrndr.shape.drawComposition
import org.openrndr.svg.saveToFile

fun main() {
    application {
        program {
            val svg = drawComposition {
                fill = null
                circle(width / 2.0 - 100.0, height / 2.0, 100.0)
                circle(width / 2.0 + 100.0, height / 2.0, 100.0)
                clipMode = ClipMode.REVERSE_DIFFERENCE
                circle(width / 2.0, height / 2.0, 100.0)
            }

            val s = svg.findShapes().map { it.shape.contours.map { it.segments }.flatten() }.flatten()
            val nonDupes = svg.dedupe()

            extend {
                drawer.clear(ColorRGBa.PINK)
                drawer.composition(nonDupes)
            }
            keyboard.keyDown.listen {
                when (it.key) {
                    KEY_SPACEBAR -> saveFileDialog(supportedExtensions = listOf("svg")) { file ->
                        nonDupes.saveToFile(file)
                    }
                }

            }
        }
    }
}

private fun Segment.contains(other: Segment, error: Double = 0.5): Boolean = this !== other &&
        this.on(other.start, error) != null &&
        this.on(other.end, error) != null &&
        this.on(other.position(1.0 / 3), error) != null &&
        this.on(other.position(2.0 / 3), error) != null

