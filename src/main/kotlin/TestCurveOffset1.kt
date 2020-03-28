import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extensions.Screenshots
import org.openrndr.math.IntVector2
import org.openrndr.math.Vector2
import org.openrndr.shape.SegmentJoin
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.contour
import kotlin.system.exitProcess

/**
 * Testing recent changes to ShapeContour and .offset()
 */

fun main() = application {
    configure {
        width = 600
        height = 600
        hideWindowDecorations = true
        position = IntVector2(10, 10)
    }

    program {
        val curves = mutableListOf<ShapeContour>()

        curves.add(
            contour {
                moveTo(Vector2(width * 0.5, height * 0.4))
                lineTo(Vector2(width * 0.6, height * 0.6))
                lineTo(Vector2(width * 0.4, height * 0.6))
                lineTo(anchor)
                close()
            }
        )

        for(i in 0..6) {
            curves.add(curves[i].offset(-50.0 + i * 7.0, SegmentJoin.BEVEL))
        }

        extend(Screenshots())

        extend {
            drawer.background(ColorRGBa.WHITE)
            drawer.stroke = ColorRGBa(0.0, 0.0, 0.0, 0.1)
            for(i in curves.size-1 downTo 0) {
                drawer.fill = ColorRGBa(i * 0.15, 0.7, 1.0 - i * 0.18, 1.0)
                drawer.contour(curves[i])
            }
        }

        keyboard.keyDown.listen {
            if (it.key == KEY_ESCAPE) {
                exitProcess(0)
            }
        }

    }
}
