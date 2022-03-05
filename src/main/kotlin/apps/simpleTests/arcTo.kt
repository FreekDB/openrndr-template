package apps.simpleTests

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.extra.noise.Random
import org.openrndr.math.Polar
import org.openrndr.shape.Circle
import org.openrndr.shape.contour

/**
 * id: 1773849f-dbe8-4db9-b9cb-9b9ffbcf3925
 * description: New sketch
 * tags: #new
 */

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
                },
                contour {
                    moveTo(50.0, 200.0)
                    arcTo(
                        150.0,
                        150.0,
                        0.0,
                        largeArcFlag = false,
                        sweepFlag = true,
                        tx = 200.0, ty = 50.0
                    )
                }
            ))

            drawer.rectangle(50.0, 50.0, 200.0, 100.0)
            drawer.rectangle(50.0, 50.0, 100.0, 200.0)
            drawer.circler(Circle(200.0, 100.0, 50.0))
            drawer.circler(Circle(100.0, 200.0, 50.0))
        }
    }
}

private fun Drawer.circler(c: Circle) {
    circle(c.scaledTo(c.radius + 1))
}
