package apps2

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extensions.Screenshots
import org.openrndr.math.CatmullRomChain2
import org.openrndr.math.Polar
import org.openrndr.math.map
import org.openrndr.shape.Segment
import org.openrndr.shape.ShapeContour

fun main() = application {
    program {
        //val points = List(9) { drawer.bounds.randomPoint() }
        val points = List(6) { Polar(it * 69.0, 200.0).cartesian + drawer.bounds.center }
        val cmr = CatmullRomChain2(points, 0.5, loop = true)
        val contour = ShapeContour.fromPoints(cmr.positions(200), true)


        extend(Screenshots())
        extend {
            drawer.run {
                clear(ColorRGBa.WHITE)
                fill = null
                stroke = ColorRGBa.BLACK
                contour(contour)
                circles(points, 5.0)

                stroke = ColorRGBa.RED
                val a = mouse.position.x.map(0.0, width * 1.0, 100.0, 3000.0)
                contour(cmr.toContour(a))
                fill = ColorRGBa.BLACK
                text(a.toString(), 50.0, 50.0)
            }
        }
    }
}

fun CatmullRomChain2.toContour(v: Double): ShapeContour {
    return ShapeContour(segments.map {
        it.run {
            val d = p1.distanceTo(p2)
            val a = v / d
            Segment(
                p1, p1 + (p2 - p0) / a,
                p2 - (p3 - p1) / a, p2
            )
        }
    }, loop)
}