package latest

import org.openrndr.applicationSynchronous
import org.openrndr.color.ColorRGBa
import org.openrndr.shape.Circle
import org.openrndr.shape.ClipMode
import org.openrndr.shape.drawComposition

fun main() {
    applicationSynchronous {
        program {
            val sizes = listOf(40, 30, 30, 40, 30, 10, 40, 40, 30, 20, 10, 10, 30, 40, 30, 40, 30, 40, 40, 30)
            val circleShapes = List(20) {
                val pos = drawer.bounds.position(0.1, 0.1).mix(
                    drawer.bounds.position(0.9, 0.9), it / 20.0
                )
                Circle(pos, sizes[it] * 1.0)
            }
            val svg = drawComposition {
                fill = null
                circle(drawer.bounds.center, 150.0)
                clipMode = ClipMode.REVERSE_DIFFERENCE
                circles(circleShapes)
            }

            extend {
                drawer.clear(ColorRGBa.PINK)
                drawer.composition(svg)
                drawer.circles(circleShapes.map { it.center }, 5.0)
            }
        }
    }
}
