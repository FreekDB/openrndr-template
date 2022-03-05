package apps4

import aBeLibs.geometry.arcContour
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.LineJoin
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.Random
import org.openrndr.extra.shapes.grid
import org.openrndr.extras.color.presets.DARK_ORANGE
import org.openrndr.extras.color.presets.ORANGE
import org.openrndr.math.transforms.transform
import org.openrndr.shape.Circle

/**
 * id: bb6d60df-09b4-45fe-b0ba-855d731eca08
 * description: New sketch
 * tags: #new
 */

/**
 * Something something
 * <p><img src="https://avatars.githubusercontent.com/u/31103334?s=200&v=4"/></p>
 */

fun main() = application {
    program {
        val grid = drawer.bounds.grid(8, 6, 50.0, 50.0)
        val arcs = grid.flatten().map {
            val start = 180 + 180 * Random.simplex(it.center * 0.001)
            val end = start * 2
            val s = Circle(it.center, 30.0).arcContour(start, end, true)
            s.transform(transform {
                translate(it.center - s.bounds.center)
            })
        }

        extend(Screenshots())
        extend {
            drawer.apply {
                clear(ColorRGBa.WHITE)
                stroke = ColorRGBa.DARK_ORANGE
                fill = ColorRGBa.ORANGE
                lineJoin = LineJoin.ROUND
                contours(arcs)
            }
        }
    }
}