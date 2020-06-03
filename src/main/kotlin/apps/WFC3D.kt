package apps

import data.Array3D
import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.loadFont
import org.openrndr.extensions.Screenshots
import org.openrndr.math.Vector2
import org.openrndr.shape.LineSegment
import org.openrndr.shape.Segment
import org.openrndr.shape.intersection
import kotlin.system.exitProcess

/**
 * Basic template
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

        keyboard.keyDown.listen {
            when (it.key) {
                KEY_ESCAPE -> exitProcess(0)
            }
        }
    }
}
