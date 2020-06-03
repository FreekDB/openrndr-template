import geometry.Human
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.DrawPrimitive
import org.openrndr.draw.VertexBuffer
import org.openrndr.draw.vertexBuffer
import org.openrndr.draw.vertexFormat
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.Random
import org.openrndr.extra.olive.oliveProgram
import org.openrndr.shape.FillRule
import org.openrndr.shape.Shape
import org.openrndr.shape.triangulate

fun main() = application {
    configure {
        width = 900
        height = 900
    }
    oliveProgram {
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
