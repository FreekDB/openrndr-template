package latest

import org.openrndr.KEY_ENTER
import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.dialogs.saveFileDialog
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.Random
import org.openrndr.math.Polar
import org.openrndr.shape.ClipMode
import org.openrndr.shape.Composition
import org.openrndr.shape.draw
import org.openrndr.shape.drawComposition
import org.openrndr.svg.saveToFile

fun main() {
    application {
        program {
            Random.seed = System.nanoTime().toString()
            fun newDesign(): Composition {
                val svg = drawComposition {
                    circle(drawer.bounds.center, 150.0)
                }
                val modes = listOf(ClipMode.REVERSE_DIFFERENCE, ClipMode.DIFFERENCE, ClipMode.UNION)
                for (i in 0 until 30) {
                    svg.draw {
                        clipMode = modes.random()
                        val pos = drawer.bounds.center + Polar(Random.int(5) * 72.0, 40.0 + i * 8).cartesian
                        circle(pos, Random.int(1, 5) * 10.0)
                    }
                }
                return svg
            }

            var svg = newDesign() //.dedupe()

            extend(Screenshots())
            extend {
                drawer.clear(ColorRGBa.PINK)
                drawer.composition(svg)
                drawer.text("$seconds", 20.0, 20.0)
            }
            mouse.buttonDown.listen {
                svg = newDesign() //.dedupe()
            }
            keyboard.keyDown.listen {
                when (it.key) {
                    KEY_ENTER -> saveFileDialog(supportedExtensions = listOf("svg")) { file ->
                        svg.saveToFile(file)
                    }
                    KEY_ESCAPE -> application.exit()
                }
            }
        }
    }
}
