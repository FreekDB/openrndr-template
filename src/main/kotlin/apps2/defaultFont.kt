package apps2

import org.openrndr.application
import org.openrndr.text.writer

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

