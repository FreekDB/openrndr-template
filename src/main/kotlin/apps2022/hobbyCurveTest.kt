package apps2022

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolated
import org.openrndr.extra.shapes.hobbyCurve
import org.openrndr.extras.color.presets.BLUE_STEEL
import org.openrndr.extras.color.presets.ORANGE_RED
import org.openrndr.math.Vector2
import org.openrndr.shape.ShapeContour

/**
 * id: 3b40143a-2c2a-4863-aa9a-63a670255a12
 * description: New sketch
 * tags: #new
 */

fun main() = application {
    configure { }
    program {
        val a = ShapeContour.fromPoints(
            listOf(
                Vector2(100.0, 100.0),
                Vector2(200.0, 300.0),
                Vector2(300.0, 100.0),
                Vector2(400.0, 300.0)
            ), false
        )

        val b = a.hobbyCurve()

        extend {
            drawer.isolated {
                clear(ColorRGBa.WHITE)
                fill = null
                strokeWeight = 6.0
                stroke = ColorRGBa.BLUE_STEEL
                contour(a)
                stroke = ColorRGBa.ORANGE_RED
                contour(b)
            }
        }
    }
}
