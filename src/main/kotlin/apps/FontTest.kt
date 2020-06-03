package apps

import org.openrndr.KEY_ARROW_DOWN
import org.openrndr.KEY_ARROW_UP
import org.openrndr.application
import org.openrndr.draw.loadFont
import org.openrndr.draw.shadeStyle
import org.openrndr.math.Vector2

/**
 * Font testing. Trying to find out why fonts look blurry.
 * I suspect some buffer is off by 1 in size, as when I move the mouse to different parts of
 * the screen different letters are sharp or blurry.
 */

fun main() = application {
    var fontSz = 8.4
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
            drawer.fontMap = font
            drawer.text("Hello there! #tags @mentions then 3+4*5-6/7...", mouse.position+0.5)
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
                changeFontSize(0.01)
            }
            if (it.key == KEY_ARROW_DOWN) {
                changeFontSize(-0.01)
            }
        }

    }
}
