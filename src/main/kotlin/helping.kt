
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.loadImage
import org.openrndr.draw.renderTarget

fun main() = application {
    configure {
        width  = 500
        height = 1000
    }

    program {
        val image = loadImage("data/images/pm5544.png")
        val rt = renderTarget(500, 500) {
            colorBuffer()
        }.apply {
            clearColor(0, ColorRGBa.PINK)
        }
        drawer.isolatedWithTarget(rt) {
            ortho(rt)
            drawer.image(image, 0.0, 0.0, 500.0, 500.0)
        }

        extend {
            drawer.image(rt.colorBuffer(0))
        }
    }
}
