package apps2

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.DrawStyle
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.uniform
import org.openrndr.extra.noise.uniforms
import org.openrndr.math.IntVector2
import org.openrndr.math.Vector2
import org.openrndr.shape.contour

fun main() = application {
    configure {
        width = 1000
        height = 1000
    }

    program {
        val points = Vector2.uniforms(400, drawer.bounds)
        val style = DrawStyle(stroke = ColorRGBa.WHITE.opacify(0.35), strokeWeight = 2.0, fill = ColorRGBa.GREEN)

        extend(Screenshots()) {
            scale = 2.0
        }

        extend {
//            drawer.drawStyle = style
//            drawer.lineSegments(points)

            val shape = contour {
                moveTo(Vector2(width / 2.0 - 150.0, height / 2.0 - 150.00))
                lineTo(cursor + Vector2(300.0, 0.0))
                lineTo(cursor + Vector2(0.0, 300.0))
                lineTo(cursor + Vector2(-300.0, 0.0))
                lineTo(anchor)
                close()
            }
            drawer.clear(ColorRGBa.PINK)
            drawer.fill = ColorRGBa.WHITE
            drawer.stroke = ColorRGBa.BLACK
            drawer.contours(listOf(shape))
        }
    }
}