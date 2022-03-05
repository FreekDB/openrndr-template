package apps2022

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.colorBuffer
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.renderTarget
import org.openrndr.extra.fx.distort.Perturb
import org.openrndr.extra.gui.GUI
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
    
/**
 * id: 6757f9f9-54a2-4bbe-b322-d52a436ad708
 * description: New sketch
 * tags: #new
 */    

fun main() = application {
    configure { }
    program {
        val gui = GUI().apply {
            compartmentsCollapsedByDefault = false
        }
        val rt = renderTarget(width, height) {
            colorBuffer()
            depthBuffer()
        }
        val withFX = colorBuffer(width, height)
        val fx = Perturb()
        extend(gui) {
            add(fx)
        }
        extend {
            drawer.isolatedWithTarget(rt) {
                translate(bounds.center)
                rotate(frameCount * 1.5)
                clear(ColorRGBa.BLACK)
                stroke = null
                fill = ColorRGBa.WHITE
                rectangle(Rectangle.fromCenter(Vector2.ZERO, 300.0))
            }

            fx.apply(rt.colorBuffer(0), withFX)

            drawer.image(withFX)
        }
    }
}
