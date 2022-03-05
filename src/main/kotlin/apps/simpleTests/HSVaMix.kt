package apps.simpleTests

import org.openrndr.application
import org.openrndr.color.ColorHSVa
import org.openrndr.color.ColorRGBa
import org.openrndr.color.mix
import org.openrndr.extensions.Screenshots

/**
 * id: fd097256-df17-4c06-ad96-4430242065de
 * description: New sketch
 * tags: #new
 */

fun main() = application {
    configure {
        width = 300
        height = 300
    }

    program {
        val left = ColorHSVa(20.0, 1.0, 1.0)
        val right = ColorHSVa(340.0, 1.0, 1.0)
        val middle = mix(left, right, 0.5)
        extend(Screenshots())
        extend {
            drawer.clear(ColorRGBa.WHITE)

            drawer.fill = left.toRGBa()
            drawer.rectangle(50.0, 50.0, 40.0, 200.0)

            drawer.fill = middle.toRGBa()
            drawer.rectangle(100.0, 50.0, 40.0, 200.0)

            drawer.fill = right.toRGBa()
            drawer.rectangle(150.0, 50.0, 40.0, 200.0)
        }
    }
}
