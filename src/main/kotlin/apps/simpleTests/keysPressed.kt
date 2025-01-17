package apps.simpleTests

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.loadFont

/**
 * id: 4e267088-34b9-4a4d-84d8-9c38d310099c
 * description: New sketch
 * tags: #new
 */

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
//                if(s.length == 1) {
                val n = s[0].code.toByte()
                drawer.text(
                    s,
                    10.0 + (n * 777.0) % (width - 50.0),
                    height - 10.0 - (n * 352.0) % (height - 40.0)
                )
//                }
            }
        }
    }
}
