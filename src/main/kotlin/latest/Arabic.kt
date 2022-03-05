package latest


import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.loadFont

/**
 * id: fe5647bc-7b88-467d-a5b3-dea72b5b290e
 * description: New sketch
 * tags: #new
 */

fun main() = application {
    program {
        val font = loadFont(
            "data/fonts/Amiri-Regular.ttf", 100.0,
            characterSet = "أميري".toCharArray().toSet()
        )

        backgroundColor = ColorRGBa.WHITE

        extend {
            drawer.fill = ColorRGBa.BLACK
            drawer.fontMap = font
            drawer.text("أميري".reversed(), 50.0, 200.0)
        }
    }
}
