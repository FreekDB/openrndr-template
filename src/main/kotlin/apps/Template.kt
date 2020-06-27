package apps

import org.openrndr.application
import org.openrndr.extensions.Screenshots

/**
 * Basic template
 */

fun main() = application {
    program {

        extend(Screenshots())
        extend {
        }
    }
}
