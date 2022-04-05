package apps

import aBeLibs.data.Array3D
import org.openrndr.application
import org.openrndr.extensions.Screenshots
import kotlin.system.exitProcess

/**
 * id: 15ed4336-8282-40d8-8ef2-d04cf21034b1
 * description: Here I planned to port https://github.com/action-script/ofxWFC3D which uses
 * Wave Function Collapse. Didn't get very far :) Just created an Array3D data type.
 * tags: #wfc
 */

fun main() = application {
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
    }
}
