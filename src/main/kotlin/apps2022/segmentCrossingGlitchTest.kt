package apps2022

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.ColorBuffer
import org.openrndr.draw.Drawer
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.renderTarget
import org.openrndr.extensions.Screenshots
import org.openrndr.math.IntVector2
import org.openrndr.shape.Rectangle
import org.openrndr.shape.Segment
    
/**
 * id: 01494348-a86b-479b-844b-51ba24bf263b
 * description: New sketch
 * tags: #new
 */

private fun Drawer.image(colorBuffer: ColorBuffer, bounds: Rectangle) =
    image(colorBuffer, bounds.x, bounds.y, bounds.width, bounds.height)

fun main() = application {
    program {
        val rt = renderTarget(width / 10, height / 10) {
            colorBuffer()
            depthBuffer()
        }
        val s = List(200) {
            Segment(
                IntVector2(it, 0).vector2,
                IntVector2(width / 10 - it, height / 10).vector2
            )
        }
        extend(Screenshots())
        extend {
            drawer.isolatedWithTarget(rt) {
                clear(ColorRGBa.WHITE)
                ortho(rt)
                stroke = ColorRGBa.BLACK//.opacify(0.5)
//                s.forEach {
//                    segment(it)
//                }
                segments(s)
            }
            drawer.image(rt.colorBuffer(0), drawer.bounds)
        }
    }
}
