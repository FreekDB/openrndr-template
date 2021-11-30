package apps5

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.renderTarget
import org.openrndr.math.Vector2

fun main() = application {
    program {
        extend {
            val a = Vector2(1.1, 2.2)
            val b = ColorRGBa.WHITE
            val rt = renderTarget(400, 400) {
                colorBuffer()
            }
            val rt2 = renderTarget(500, 400) {

            }
            drawer.isolatedWithTarget(rt) {

            }
        }
    }
}