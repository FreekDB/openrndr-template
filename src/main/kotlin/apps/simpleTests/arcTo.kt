package apps.simpleTests

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.noise.Random
import org.openrndr.math.Polar
import org.openrndr.shape.contour

fun main() = application {
    program {
        extend {
            backgroundColor = ColorRGBa.WHITE

            val angle = Random.double0(360.0)
            val diameter = 130.0
            val end = Polar(seconds * 10.0, diameter * 2).cartesian

            drawer.stroke = ColorRGBa.BLACK
            drawer.fill = null
            drawer.lineSegment(drawer.bounds.center, drawer.bounds.center + end)

            drawer.contours(listOf(
                contour {
                    moveTo(drawer.bounds.center)
                    arcTo(
                        diameter * 2.0, // only effect if > 1.0
                        diameter * 2.0, // only effect if > 1.0
                        angle, // no effect
                        largeArcFlag = false, // no effect
                        sweepFlag = false, // side
                        end = drawer.bounds.center + end
                    )
                },
                contour {
                    moveTo(drawer.bounds.center)
                    arcTo(
                        diameter,
                        diameter,
                        angle, // no effect
                        largeArcFlag = true, // no effect
                        sweepFlag = true, // side
                        end = drawer.bounds.center - end
                    )
                }
            ))
        }
    }
}
