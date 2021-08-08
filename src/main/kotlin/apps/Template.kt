package apps

import org.openrndr.applicationSynchronous
import org.openrndr.extensions.Screenshots

/**
 * Basic template
 */

fun main() = applicationSynchronous {
    program {

        extend(Screenshots())
        extend {
        }
    }
}
