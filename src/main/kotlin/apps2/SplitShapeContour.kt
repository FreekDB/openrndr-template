package apps2

import aBeLibs.geometry.bend
import org.openrndr.application
import org.openrndr.color.ColorHSLa
import org.openrndr.color.ColorRGBa
import org.openrndr.math.Polar
import org.openrndr.shape.Circle
import org.openrndr.shape.Rectangle
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.contour
import kotlin.math.PI
import kotlin.math.cos

/**
 * Example of splitting ShapeContours with a ShapeContour
 */

fun main() = application {
    program {

        val lines = mutableListOf<ShapeContour>()
        val center = drawer.bounds.center

        for (w in 150 until 250 step 20) {
            lines.add(Rectangle.fromCenter(center, w * 1.0, w * 1.0).contour)
        }
        lines.add(Circle(center, 65.0).contour.sampleEquidistant(80))

        extend {
            val a = frameCount * 0.02
            val knife = contour {
                moveTo(center + Polar(Math.toDegrees(a),
                        250.0).cartesian)
                curveTo(
                        center + Polar(Math.toDegrees(a + 1),
                                250.0).cartesian,
                        center + Polar(Math.toDegrees(a + 1 + PI),
                                100 + 100 * cos(a)).cartesian,
                        center + Polar(Math.toDegrees(a + PI),
                                100 + 100 * cos(a)).cartesian
                )
            }.sampleEquidistant(80)

            val cutLines = lines.map { it.split(knife) }.flatten()
                    .bend(knife)

            drawer.run {
                clear(ColorRGBa.WHITE)
                fill = null
                stroke = ColorRGBa.GRAY
                contour(knife)
                cutLines.forEachIndexed { i, line ->

                    strokeWeight = 4.0
                    stroke = ColorHSLa((i * 110) % 360.0, 1.0,
                            0.25 + (i % 2) * 0.5).toRGBa()
                    contour(line)

                    strokeWeight = 1.0
                    stroke = ColorRGBa.GRAY
                    circle(line.position(0.0), 10.0)
                }
            }
        }
    }
}

