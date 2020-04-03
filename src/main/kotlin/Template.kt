import org.openrndr.*
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolated
import org.openrndr.draw.loadFont
import org.openrndr.extensions.Screenshots
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.shape.SegmentJoin
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.contour
import org.openrndr.text.writer
import java.lang.Math.abs
import kotlin.system.exitProcess

/**
 * Basic template
 */

fun main() = application {
    configure {
        width = 1500
        height = 800
    }

    program {
        val font = loadFont("data/fonts/IBMPlexMono-Regular.ttf", 24.0)

        extend(Screenshots())

        extend {
            with(drawer) {
                background(ColorRGBa.WHITE)
                stroke = ColorRGBa(0.0, 0.0, 0.0, 0.05)
                fill = ColorRGBa.PINK
                fontMap = font
            }

            drawer.isolated {
                translate(width * 0.5, height * 0.5)
                drawer.circle(Vector2.ZERO, 200.0)
            }
        }

        keyboard.keyDown.listen {
            when (it.key) {
                KEY_ESCAPE -> exitProcess(0)
            }
        }
    }
}
