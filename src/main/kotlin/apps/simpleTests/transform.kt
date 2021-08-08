package apps.simpleTests

import org.openrndr.applicationSynchronous
import org.openrndr.math.transforms.transform
import org.openrndr.shape.Circle

fun main() = applicationSynchronous {
    program {

        extend {
//            var t = transform {
//                translate(drawer.bounds.center)
//                rotate(Vector3.UNIT_Z,45.0)
//                translate(0.0, 100.0)
//            }
//
//            t *= transform { rotate(Vector3.UNIT_Z, 45.0)}
//
//            drawer.view *= t
//            drawer.rectangle(Rectangle.fromCenter(Vector2.ZERO, 20.0, 200.0))

            val t = transform {
                scale(0.5, 1.0, 1.0)
                translate(200.0, 0.0)
            }

            val r = 350.0
            val oval = Circle(drawer.bounds.position(0.0, 0.0), r).shape
            val s = oval.transform(t)
            drawer.shape(s)
        }
    }
}
