package apps2

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.LineJoin
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.shape.LineSegment
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.intersections
import java.text.DecimalFormat
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

fun main() = application {
    program {
        val points = 200
        val dec = DecimalFormat("0.000")
//        extend(ScreenRecorder())
        extend {
            val contour = ShapeContour.fromPoints(
                List(points) {
                    val a = PI * 2 * it / points
                    val x = (200 + 50 * cos(a * 2)) * sin(a * 3 + sin(a))
                    //val x = 200 * sin(a)
                    val y = 150 * cos(a * 2 + seconds * 0.2)
                    Vector2(x, y)
                }, closed = true
            )
            val ints = intersections(contour, contour)
            drawer.run {
                clear(ColorRGBa.WHITE)
                fill = ColorRGBa.BLACK
                ints.forEachIndexed { i, it ->
                    text(
                        "${dec.format(it.a.contourT)} ${dec.format(it.b.contourT)}",
                        20.0, 40.0 + i * 40.0
                    )
                }

                translate(width * 0.5, height * 0.5)
                fill = null
                stroke = ColorRGBa.BLACK
                lineJoin = LineJoin.ROUND
                contour(contour)
                fill = ColorRGBa.PINK.opacify(0.3)
                circles(ints.map { it.position }, 10.0)
                lineSegments(ints.map {
                    LineSegment(
                        it.position,
                        it.position + Polar(it.a.contourT * 360, 50.0)
                            .cartesian
                    )
                })
            }
//            if(seconds * 0.2 >= PI * 2) {
//                application.exit()
//            }
        }
    }
}