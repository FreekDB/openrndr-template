package latest

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.noise.poissonDiskSampling
import org.openrndr.math.Matrix44
import org.openrndr.math.transforms.scale
import org.openrndr.math.transforms.translate
import org.openrndr.shape.Rectangle
import org.openrndr.shape.Shape
import org.openrndr.shape.contains
import org.openrndr.svg.loadSVG

/**
 * id: 97d9bda0-5da4-46e5-b167-09fdd2e807db
 * description: New sketch
 * tags: #broken
 */

fun main() = application {
    program {
        val m = Matrix44.scale(0.5, 0.5, 1.0) *
                Matrix44.translate(100.0, 100.0, 0.0)
        val comp = loadSVG("data/butterfly.svg")
        // It seems like this specific SVG has the contours reversed so I need to fix that
        // otherwise the point detection happens only in the wholes.
        val butterfly = Shape(comp.findShapes()[0].shape.transform(m).contours.map { it/*.reversed*/ })
        val square = Rectangle.fromCenter(drawer.bounds.position(0.75, 0.5), 100.0).shape

        val points = poissonDiskSampling(drawer.bounds, 10.0, 20)

        extend {
            drawer.clear(ColorRGBa.PINK)
            drawer.stroke = null
            drawer.strokeWeight = 0.0
            points.forEach { p ->
                drawer.fill = when (p) {
                    in butterfly -> ColorRGBa.RED
                    in square -> ColorRGBa.BLUE
                    else -> ColorRGBa.BLACK.opacify(0.2)
                }
                drawer.circle(p, 5.0)
            }

            drawer.apply {
                fill = ColorRGBa.BLACK.opacify(0.1)
                stroke = ColorRGBa.BLACK
                shape(square)
                shape(butterfly)
            }
        }
    }
}
