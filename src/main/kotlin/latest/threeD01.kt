package latest

import org.openrndr.WindowMultisample

import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolated
import org.openrndr.extra.compositor.blend
import org.openrndr.extra.compositor.compose
import org.openrndr.extra.compositor.draw
import org.openrndr.extra.compositor.layer
import org.openrndr.extra.fx.blend.Add
import org.openrndr.extras.camera.Orbital
import org.openrndr.math.Vector2
import org.openrndr.math.Vector3
import org.openrndr.shape.Rectangle
import kotlin.math.PI
import kotlin.math.sin

/**
 * In this example I'm testing if it makes sense to use Blend Mode ADD
 * for 3D, as the drawing is currently meant to be used in 2D only.
 * So I'm testing if there are use cases in which 3D is usable.
 */
fun main() = applicationSynchronous {
    configure {
        multisample = WindowMultisample.SampleCount(4)
    }
    program {

        val cam = Orbital()
        cam.eye = Vector3.UNIT_Z * 150.0
        cam.camera.depthTest = false

        fun drawWalls(color: ColorRGBa) {
            drawer.isolated {
                stroke = color
                translate(Vector3.UNIT_Z * -50.0)
                rectangle(Rectangle.fromCenter(Vector2.ZERO, 100.0))
                translate(Vector3.UNIT_Z * 100.0)
                rectangle(Rectangle.fromCenter(Vector2.ZERO, 100.0))
            }
        }

        val composite = compose {
            draw {
                drawer.fill = ColorRGBa.PINK
                drawer.stroke = null
                drawer.circle(sin(seconds * PI * 0.5) * 100.0, 0.0, 175.0)
            }

            layer {
                blend(Add())
                draw {
                    // NOTE: when drawing in the layer the multiSample
                    // setting no longer has effect and the 3D shape
                    // looks aliased.
                    drawer.fill = null
                    drawer.strokeWeight = 1.0
                    drawWalls(ColorRGBa.GRAY)
                    drawer.rotate(Vector3.UNIT_X, 90.0)
                    drawWalls(ColorRGBa.GRAY)
                    drawer.rotate(Vector3.UNIT_Y, 90.0)
                    drawWalls(ColorRGBa.GRAY)
                }
            }
        }

        extend(cam)
        extend {
            composite.draw(drawer)
        }
    }
}
