package apps2.simpleTests

import org.openrndr.application
import org.openrndr.draw.writer

/**
 * id: d045e587-6b53-40af-888c-d2c724cca2ff
 * description: Test the default font (new feature). Broken in OPENRNDR 0.4
 * tags: #broken
 */

fun main() = application {
    program {
        extend {
            drawer.translate(50.0, 50.0)
            drawer.writer {
                newLine()
                text("One line")
                newLine()
                text("Another line")
            }
            drawer.text("Hello", 250.0, 50.0)
        }
    }
}
