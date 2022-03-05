package latest

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolated
import org.openrndr.math.Vector2
import org.openrndr.shape.Circle
import org.openrndr.shape.Shape
import org.openrndr.shape.contour
import org.openrndr.shape.difference

/**
 * id: 87a1ad7a-454b-49f9-b4a4-587dd7df06e2
 * description: Remove part of a line using a circle.
 * ShapeContour (line) - Shape (circle) = ShapeContour with missing part
 * [-----] ==> [-o---] ==> [- ---]
 * tags: #new
 */

fun main() = application {
    program {
        val curve = contour {
            moveTo(Vector2.ZERO)
            curveTo(
                Vector2(0.0, -80.0),
                Vector2(40.0, 100.0),
                Vector2(40.0, 200.0)
            )
        }
        val deleter = Circle(Vector2.ZERO, 15.0).shape
        val result = curve.shape - deleter
        extend {
            drawer.apply {
                clear(ColorRGBa.WHITE)
                fill = ColorRGBa.BLACK

                translate(150.0, 150.0)
                text("original curve", -50.0, -30.0)
                contour(curve)

                translate(150.0, 0.0)
                text("difference()", -50.0, -30.0)
                isolated {
                    fill = null
                    shape(deleter)
                }
                shape(result)

                translate(150.0, 0.0)
                text("result", -50.0, -30.0)
                shape(result)
            }
        }
    }
}

private operator fun Shape.minus(other: Shape) = difference(this, other)
