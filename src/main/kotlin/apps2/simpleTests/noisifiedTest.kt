package apps2.simpleTests

import aBeLibs.geometry.noisified
import org.openrndr.application

import org.openrndr.color.ColorRGBa
import org.openrndr.shape.Circle
import org.openrndr.shape.ShapeContour

/**
 * id: c963f331-4208-404f-89e9-e493b6412b7b
 * description: New sketch
 * tags: #new
 */

fun main() = application {
    program {
        val c = ShapeContour.fromPoints(
            Circle(drawer.bounds.center, 200.0).contour
                .equidistantPositions(100), true
        )
        val c2 = c.noisified(20.0, true, 0.003)

        extend {
            drawer.fill = null
            drawer.stroke = ColorRGBa.WHITE
            drawer.contour(c)
            drawer.contour(c2)
        }
    }
}
