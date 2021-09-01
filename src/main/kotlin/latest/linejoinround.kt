package latest

import aBeLibs.geometry.randomPoint
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.math.Matrix44
import org.openrndr.math.Vector2
import org.openrndr.math.transforms.scale
import org.openrndr.math.transforms.translate
import org.openrndr.shape.Rectangle
import org.openrndr.shape.contains
import org.openrndr.svg.loadSVG

fun main() = application {

    program {
        val butterfly =
            loadSVG("/tmp/butterfly.svg").findShapes()[0].effectiveShape
                .transform(
                Matrix44.scale(0.25, 0.25, 1.0) * Matrix44.translate(
                    300.0,
                    500.0,
                    0.0
                )
            )
        val square = Rectangle.fromCenter(
            Vector2(0.75 * width, height / 2.0),
            100.0
        ).shape
        val points = List(1000) { drawer.bounds.randomPoint() }
        extend {
            drawer.clear(ColorRGBa.PINK)
            points.forEach { p ->
                drawer.apply {
                    stroke = null
                    strokeWeight = 0.0
                    fill = ColorRGBa.BLACK
                    if (butterfly.closedContours.any { it.contains(p) }) {
                        fill = ColorRGBa.RED
                    } else if (square.contains(p)) {
                        fill = ColorRGBa.BLUE
                    }
                    circle(p, 5.0)
                }
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

