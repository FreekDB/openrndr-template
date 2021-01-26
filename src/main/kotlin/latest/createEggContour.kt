package latest

import org.openrndr.application
import org.openrndr.extra.noise.Random
import org.openrndr.math.CatmullRomChain2
import org.openrndr.math.Polar
import org.openrndr.math.transforms.transform
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.toContour

fun main() = application {
    program {
        val contours = mutableListOf<ShapeContour>()

        fun doit() {
            val points = List(5) { it * 72.0 + Random.double0(70.0) }.map {
                Polar(it, 100.0).cartesian
            }
            contours.clear()
            val pos1 =
                transform { translate(drawer.bounds.position(0.25, 0.5)) }
            val pos2 =
                transform { translate(drawer.bounds.position(0.75, 0.5)) }
            contours.add(
                CatmullRomChain2(points, 0.4, true).toContour()
                    .transform(pos1)
            )
            contours.add(
                CatmullRomChain2(points, 0.5, true).toContour()
                    .transform(pos2)
            )
        }
        doit()

        extend {
            drawer.apply {
                stroke = null
                contours(contours)
            }
        }
        keyboard.keyDown.listen {
            doit()
        }
    }
}
