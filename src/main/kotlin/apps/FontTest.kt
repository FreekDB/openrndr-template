import org.openrndr.KEY_ARROW_DOWN
import org.openrndr.KEY_ARROW_UP
import org.openrndr.application
import org.openrndr.draw.loadFont
import org.openrndr.draw.shadeStyle

/**
 * Font testing. Trying to find out why fonts look blurry.
 */

fun main() = application {
    var fontSz = 12.0
    configure {
        width = 768
        height = 576
    }

    program {
        var font = loadFont(
            "file:/home/funpro/src/OR/openrndr-template/data/fonts/slkscr.ttf",
            fontSz
        )

        extend {
            drawer.shadeStyle = shadeStyle {
                fragmentTransform = """x_fill.gb = vec2(x_fill.r);"""
            }
            drawer.image(font.texture)
        }

        fun changeFontSize(inc: Double) {
            fontSz += inc
            font = loadFont(
                "file:/home/funpro/src/OR/openrndr-template/data/fonts/slkscr.ttf",
                fontSz
            )
            println(fontSz)
        }

        keyboard.keyDown.listen {
            if (it.key == KEY_ARROW_UP) {
                changeFontSize(0.1)
            }
            if (it.key == KEY_ARROW_DOWN) {
                changeFontSize(-0.1)
            }
        }

    }
}
