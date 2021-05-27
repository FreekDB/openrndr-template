package latest

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.loadFont

fun main() = application {
    program {
        val font = loadFont("data/fonts/Amiri-Regular.ttf", 100.0,
            characterSet = "أميري".toCharArray().toSet())

        backgroundColor = ColorRGBa.WHITE

        extend {
            drawer.fill = ColorRGBa.BLACK
            drawer.fontMap = font
            drawer.text("أميري".reversed(), 50.0, 200.0)
        }
    }
}
