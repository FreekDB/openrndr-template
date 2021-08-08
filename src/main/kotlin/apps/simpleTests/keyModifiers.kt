package apps.simpleTests

import org.openrndr.applicationSynchronous

fun main() {
    applicationSynchronous {
        program {
            mouse.dragged.listen {
                println(it.modifiers)
            }
        }
    }
}
