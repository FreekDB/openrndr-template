package apps2

import geometry.split
import org.openrndr.application
import org.openrndr.color.ColorHSLa
import org.openrndr.color.ColorRGBa
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.shape.*
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

//        extend(ScreenRecorder()) {
//            profile = GIFProfile()
//            maximumFrames = 314
//            outputFile = "/tmp/vid/split3.gif"
//        }
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

            val cutLines = lines.map { it.split(knife) }.flatten().bend(knife)

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

/**
 * TODO: bend line
 * - if the line starts and ends in the same place, bend both ends.
 * - bend: find points that are close enough to the knife,
 * then rotate them repeatedly in a shrinking selection
 * (rotate 50 points around the first one, then 49, 48, 47).
 * - bend strength depends on the distance to the knife
 * and the distance to the beginning
*/
private fun List<ShapeContour>.bend(knife: ShapeContour) = this.map {
    val points = mutableListOf<Vector2>()
    val tmp = Vector2(5.0, 0.0)
    tmp.rotate(5.0, Vector2.ZERO)
    //it.equidistantPositions()
    ShapeContour.fromPoints(points, false)
}


