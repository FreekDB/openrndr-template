import editablecurve.intersects
import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolated
import org.openrndr.draw.loadFont
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.palette.PaletteStudio
import org.openrndr.extra.shapes.regularStar
import org.openrndr.ffmpeg.ScreenRecorder
import org.openrndr.ffmpeg.VideoWriter
import org.openrndr.math.IntVector2
import org.openrndr.math.Vector2
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.contour
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

fun main() = application {
    configure {
        width = 600
        height = 150
        hideWindowDecorations = true
    }

    program {
        val font = loadFont("/home/funpro/.local/share/fonts/NovaMono.ttf", 100.0)

        extend {
            backgroundColor = ColorRGBa.WHITE
            drawer.fontMap = font
            drawer.fill = ColorRGBa.PINK
            program.keyboard.pressedKeys.forEach { s ->
                if(s.length == 1) {
                    val n = s[0].toByte()
                    drawer.text(s,
                        10.0 + (n * 777.0) % (width - 50.0),
                        height - 10.0 - (n * 352.0) % (height - 40.0))
                }
            }
        }
    }
}
