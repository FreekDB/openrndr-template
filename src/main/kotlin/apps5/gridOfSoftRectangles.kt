package apps5

import aBeLibs.geometry.smoothed
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.Random
import org.openrndr.extra.shapes.grid
import org.openrndr.extra.shapes.hobbyCurve
import org.openrndr.extras.color.presets.LIGHT_SKY_BLUE
import org.openrndr.shape.ShapeContour

/**
 * id: 01da6a33-0a6a-43d5-ae5e-a2276621a723
 * description: Grid of soft rectangles
 * tags: #new
 */

fun main() = application {
    configure {
        width = 800
        height = 800
    }
    program {
        Random.seed = System.currentTimeMillis().toString()
        val rects = drawer.bounds
            .grid(8, 8, 100.0, 100.0, 10.0, 10.0)
            .flatten()
            .filter { Random.bool(0.95) }
            .map { rect ->
                val points = List(45) {
                    Random.double(0.0, 1.0)
                }.sorted().map { rect.contour.position(it) }
                //hobbyCurve(points, true)
                //ShapeContour.fromPoints(points, true)
                //ShapeContour.fromPoints(points, true).symmetrizeSimple()
                ShapeContour.fromPoints(points, true).smoothed(2).hobbyCurve()
            }

        extend(Screenshots())
        extend {
            drawer.clear(ColorRGBa.LIGHT_SKY_BLUE)
            drawer.stroke = ColorRGBa.WHITE
            drawer.contours(rects)
        }
    }
}
