package apps2.simpleTests

import org.openrndr.application
import org.openrndr.text.writer

/**
 * Test the default font (new feature)
 */
fun main() = application {
    program {
        extend {
            drawer.translate(50.0, 50.0)
            writer {
                newLine()
                text("One line")
                newLine()
                text("Another line")
            }
            drawer.text("Hello", 250.0, 50.0)
        }
    }
}

