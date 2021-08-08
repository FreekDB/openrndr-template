package apps2.simpleTests

import org.openrndr.applicationSynchronous
import org.openrndr.draw.writer

/**
 * Test the default font (new feature)
 */
fun main() = applicationSynchronous {
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

