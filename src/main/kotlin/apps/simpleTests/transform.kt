package apps.simpleTests

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.renderTarget
import org.openrndr.extra.noise.uniform
import org.openrndr.extra.olive.oliveProgram
import org.openrndr.math.Vector2
import org.openrndr.math.Vector3
import org.openrndr.math.transforms.transform
import org.openrndr.shape.Circle
import org.openrndr.shape.Rectangle
import org.openrndr.shape.Shape

fun main() = application {
    program {

        extend {

            var t = transform {
                translate(drawer.bounds.center)
                rotate(Vector3.UNIT_Z,45.0)
                translate(0.0, 100.0)
            }

            t *= transform { rotate(Vector3.UNIT_Z, 45.0)}

            drawer.view *= t
            drawer.rectangle(Rectangle.fromCenter(Vector2.ZERO, 20.0, 200.0))
        }
    }
}
