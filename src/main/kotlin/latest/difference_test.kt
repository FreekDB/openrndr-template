package latest

import org.openrndr.application
import org.openrndr.shape.Circle
import org.openrndr.shape.compound

/**
 * id: 5a0172da-4846-437d-a78f-343c3ad235e4
 * description: New sketch
 * tags: #new
 */

data class Vec2(val x: Double, val y: Double)

fun main() = application {
    program {
        configure {
            width = 900
            height = 900
        }

        val a = Vec2(2.3, 3.5)
        val b = Vec2(2.3, 3.5)
        println(a == b)

        val co = compound {
            difference {
                shape(Circle(185.0, height / 2.0 - 80.0, 100.0).shape)
                shape(Circle(185.0, height / 2.0 + 80.0, 100.0).shape)
            }
        }.first()

        extend {
            drawer.shape(co)
        }
    }
}
