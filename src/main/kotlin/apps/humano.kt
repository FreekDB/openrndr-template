package apps

import aBeLibs.geometry.Human
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.DrawPrimitive
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.Random

fun main() = application {
    configure {
        width = 900
        height = 900
    }
    program {
        Random.seed = System.currentTimeMillis().toString()

        val human = Human(width, height)

        extend(Screenshots())
        extend {
            drawer.clear(ColorRGBa.PINK)
            drawer.stroke = null
            drawer.vertexBuffer(human.buffer(), DrawPrimitive.TRIANGLES)
        }
        keyboard.keyDown.listen {
            if (it.name == "n") {
                human.randomize()
            }
        }
    }
}
