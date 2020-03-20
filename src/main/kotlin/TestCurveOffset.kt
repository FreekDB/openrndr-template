import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorHSVa
import org.openrndr.color.ColorRGBa
import org.openrndr.extensions.Screenshots
import org.openrndr.math.IntVector2
import org.openrndr.math.Vector2
import org.openrndr.shape.SegmentJoin
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.contour

/**
 * In this program I'm testing recent changes by Edwin to ShapeContour
 * related to the .offset() method
 */

fun main() = application {
    configure {
        width = 600
        height = 600
        hideWindowDecorations = true
        position = IntVector2(10, 10)
    }

    program {
        var curves = mutableListOf<ShapeContour>()
        curves.add(contour {
            moveTo(Vector2(180.0, 280.0))
            continueTo(Vector2(380.0, 380.0))
            continueTo(Vector2(360.0, 280.0))
            continueTo(Vector2(180.0, 280.0))
            close()
        })
        for(i in 0..6) {
            curves.add(curves[i].offset(15.0, SegmentJoin.MITER))
        }

        extend(Screenshots())

        extend {
            drawer.background(ColorRGBa.WHITE)
            curves.forEachIndexed { i, s ->
                drawer.fill = ColorRGBa(0.0, 0.0, 0.0, 0.1)
                drawer.contour(s)
                drawer.fill = null
                drawer.rectangle(s.bounds)
             }
        }

        keyboard.keyDown.listen {
            if (it.key == KEY_ESCAPE) {
                application.exit()
            }
        }

    }
}
