package apps2.rubick2D

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.math.Vector2

const val columns = 5
const val rows = 5
const val cellCount = 13

fun main() = application {
    configure {
        width = 1000
        height = 1000
    }

    program {
        val grid = Grid()

        extend {
            drawer.clear(ColorRGBa.WHITE.shade(0.25))
            drawer.translate(drawer.bounds.center)
            drawer.circle(Vector2.ZERO, 515.0 / 2) // 360.0
            grid.draw(drawer)
        }
        keyboard.keyDown.listen {
            if (it.name == "enter") {
                //grid = Grid()
                grid.moveOne()
            }
            if (it.name == "a") {
                grid.moveSome()
            }

        }
    }
}