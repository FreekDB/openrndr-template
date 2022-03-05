package apps2.simpleTests


import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.color.rgb
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.renderTarget

/**
 * id: 49270065-6248-4efb-89c6-18443b7fd679
 * description: New sketch
 * tags: #new
 */

fun main() = application {
    configure {
        width = 400
        height = 200
    }
    program {
        val canvas = renderTarget(190, 190) {
            colorBuffer()
        }
        drawer.isolatedWithTarget(canvas) {
            //ortho(canvas) // changes projection matrix
            clear(ColorRGBa.PINK)
            circle(190.0, 190.0, 20.0)
        }
        extend {
            drawer.clear(rgb(0.7))
            drawer.image(canvas.colorBuffer(0), 5.0, 5.0)
        }
    }
}
