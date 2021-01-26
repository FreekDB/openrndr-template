package apps2

import aBeLibs.geometry.*
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.noise.Random
import org.openrndr.math.Vector2
import org.openrndr.shape.Circle
import org.openrndr.shape.ShapeContour

fun main() = application {
    program {
        Random.seed = "1"

        val contours = MutableList(10) {
            val r = Random.double(50.0, 250.0)
            Circle(drawer.bounds.randomPoint(), r)
                    .contour.sampleEquidistant(r.toInt() * 2)
        }.removeIntersections(10.0)

        extend {
            drawer.run {
                clear(ColorRGBa.WHITE)
                stroke = ColorRGBa.BLACK
                fill = null
                contours(contours)
            }
        }
    }
}

