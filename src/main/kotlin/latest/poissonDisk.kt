package latest

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.noise.poissonDiskSampling
import org.openrndr.math.Polar
import org.openrndr.shape.Circle
import org.openrndr.shape.Rectangle
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.contains

/**
 * id: b18014a0-a196-4edc-b941-6a0d56b3591b
 * description: points randomly distributed in a pentagon using [poissonDiskSampling]
 * tags: #simple
 */

fun main() = application {
    program {
        val poissonArea = Rectangle(0.0, 0.0, 200.0, 200.0)
        val shp = ShapeContour.fromPoints(
            List(5) {
                Polar(it * 72.0, 100.0).cartesian +
                        poissonArea.center
            }, true
        )
        val points = poissonDiskSampling(
            poissonArea,
            5.0, 20
        ) {
            shp.contains(it)
        }

        val theCircles = points.map { Circle(it, 3.0) }

        extend {
            drawer.clear(ColorRGBa.BLACK)
            drawer.stroke = null
            drawer.fill = ColorRGBa.PINK
            drawer.translate(drawer.bounds.center)
            drawer.circles(theCircles)
        }
    }
}
