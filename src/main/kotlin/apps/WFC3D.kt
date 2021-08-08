package apps

import aBeLibs.data.Array3D
import org.openrndr.KEY_ESCAPE
import org.openrndr.applicationSynchronous
import org.openrndr.extensions.Screenshots
import kotlin.system.exitProcess

/**
 * Basic template
 */

fun main() = applicationSynchronous {
    configure {
        width = 1500
        height = 800
    }

    program {
        val numbers = Array3D(5, 5, 5, 0.0)
        numbers[2, 2, 2] = 0.5
        println(numbers)

        extend(Screenshots())
        extend {
        }

        keyboard.keyDown.listen {
            when (it.key) {
                KEY_ESCAPE -> exitProcess(0)
            }
        }
    }
}
