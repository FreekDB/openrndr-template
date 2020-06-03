package simpleTests

import org.openrndr.application

fun main() {
    application {
        program {
            mouse.dragged.listen {
                println(it.modifiers)
            }
        }
    }
}