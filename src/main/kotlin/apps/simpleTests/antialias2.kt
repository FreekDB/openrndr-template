package apps.simpleTests

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.BufferMultisample
import org.openrndr.draw.colorBuffer
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.renderTarget
import org.openrndr.math.Vector2

fun main() =
    application {
        program {
            // -- build a render target with a single color buffer attachment
            val rt = renderTarget(width, height, multisample = BufferMultisample.SampleCount(8)) {
                colorBuffer()
                depthBuffer()
            }

            val resolved = colorBuffer(width, height)

            extend {
                drawer.isolatedWithTarget(rt) {
                    drawer.clear(ColorRGBa.BLACK)
                    
                    drawer.fill = ColorRGBa.WHITE
                    drawer.stroke = null
                    drawer.circle(Vector2.ZERO, 400.0)
                }

                // -- resolve the render target attachment to `resolved`
                rt.colorBuffer(0).copyTo(resolved)

                // draw the backing color buffer to the screen
                drawer.image(resolved)

                // draw a second circle with no multisampling to compare
                drawer.fill = ColorRGBa.WHITE
                drawer.stroke = null
                drawer.circle(width * 1.0, height * 1.0, 400.0)
            }
        }
    }
