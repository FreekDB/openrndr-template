package latest

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.noise.Random
import org.openrndr.math.Polar
import org.openrndr.shape.ClipMode
import org.openrndr.shape.Composition
import org.openrndr.shape.draw
import org.openrndr.shape.drawComposition

/**
 * id: 7b9896a8-a6ea-4375-b9e4-327865a0b141
 * description: New sketch
 * tags: #new
 */

fun main() = application {
    program {
        Random.seed = System.nanoTime().toString()
        fun newDesign(): Composition {
            val svg = drawComposition {
                circle(drawer.bounds.center, 150.0)
            }
            val radii = List(30) { Random.int(1, 5) }
            val types = List(30) { Random.bool(0.8) }
            val angles = List(30) { Random.double0(360.0) }
            for (i in 0 until 30) {
                svg.draw {
                    clipMode =
                        if (types[i]) ClipMode.REVERSE_DIFFERENCE_GROUP else ClipMode.DIFFERENCE
                    val pos = drawer.bounds.center + Polar(
                        angles[i],
                        160.0
                    ).cartesian
                    circle(pos, radii[i] * 10.0)
                }
            }
            return svg
        }

        var svg = newDesign()

        extend {
            drawer.clear(ColorRGBa.PINK)
            drawer.composition(svg)
            drawer.text("$seconds", 20.0, 20.0)
        }
        mouse.buttonDown.listen {
            svg = newDesign()
        }
    }
}
