package latest

import org.openrndr.applicationSynchronous
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.noise.poissonDiskSampling
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.shape.Circle
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.contains

fun main() {
    applicationSynchronous {
        program {
            val poissonArea = Vector2(200.0, 200.0)
            val shp = ShapeContour.fromPoints(
                List(5) {
                    Polar(it * 72.0, 100.0).cartesian +
                            poissonArea / 2.0
                }, true
            )
            val points = poissonDiskSampling(
                poissonArea.x,
                poissonArea.y,
                5.0, 20
            ) { _: Double, _: Double, p: Vector2 ->
                shp.contains(p)
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
}