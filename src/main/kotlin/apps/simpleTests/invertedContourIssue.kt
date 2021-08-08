package apps.simpleTests

import org.openrndr.applicationSynchronous
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.renderTarget
import org.openrndr.extra.noise.uniform
import org.openrndr.math.Vector2
import org.openrndr.shape.Circle
import org.openrndr.shape.Shape

fun main() = applicationSynchronous {
    program {
        val bw = renderTarget(width, height) {
            colorBuffer()
            depthBuffer()
        }.apply {
            clearDepth(0.0)
        }

        extend {
            drawer.image(bw.colorBuffer(0))
            drawer.fill = ColorRGBa.PINK
            drawer.stroke = null
            drawer.shape(Circle(drawer.bounds.center, 50.0).shape)
        }

        keyboard.keyDown.listen {
            drawer.isolatedWithTarget(bw) {
                fill = ColorRGBa.PINK
                stroke = null
                shape(Shape(listOf(Circle(Vector2.uniform(Vector2.ZERO, bounds.dimensions), 100.0).contour)))
            }
        }
    }
}
