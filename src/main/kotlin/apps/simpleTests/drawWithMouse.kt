package apps.simpleTests

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.renderTarget

fun main() = application {
    program {
        val rt = renderTarget(width, height) {
            colorBuffer()
            depthBuffer()
        }.apply {
            clearDepth(0.0)
        }

        extend {
            drawer.clear(ColorRGBa.BLACK)
            drawer.image(rt.colorBuffer(0))
        }
        mouse.dragged.listen {
            drawer.isolatedWithTarget(rt) {
                stroke = ColorRGBa.WHITE
                lineSegment(it.position, it.position - it.dragDisplacement)
            }
        }
    }
}
