package intersections

import aBeLibs.geometry.*
import org.openrndr.applicationSynchronous
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.noise.Random
import org.openrndr.shape.Circle

/**
 * TODO: Fix this program
 * The behavior changed at some point
 * For each intersection I should modify only one of the two involved curves
 * deleting the area around the intersection
 */
fun main() = applicationSynchronous {
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

