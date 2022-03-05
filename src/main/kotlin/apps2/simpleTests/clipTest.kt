package apps2.simpleTests

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.color.rgba
import org.openrndr.draw.loadFont
import org.openrndr.drawComposition
import org.openrndr.math.Vector2
import org.openrndr.shape.ClipMode

/**
 * id: cd82340b-6f95-4de7-8be5-48c3dec8458a
 * description: New sketch
 * tags: #new
 */

fun main() = application {
    configure {
        width = 400
        height = 900
    }
    program {
        val font = loadFont("data/fonts/IBMPlexMono-Regular.ttf", 16.0)
        val svgs = ClipMode.values().mapIndexed { n, mode ->
            val x = width * 0.7
            val y = 50 + n * 100.0
            val r = 40.0
            drawComposition {
                fill = rgba(0.9, 0.9, 0.9, 0.7)
                circle(x - 50.0, y, r)
                circle(x + 50.0, y, r)
                lineSegment(x - 50.0, y, x + 50.0, y)
                clipMode = mode
                fill = ColorRGBa.PINK.opacify(0.8)
                circle(x, y, r)
            }
        }

        extend {
            drawer.clear(ColorRGBa.WHITE)
            drawer.fontMap = font
            svgs.forEachIndexed { n, svg ->
                drawer.composition(svg)
                drawer.fill = ColorRGBa.BLACK
                drawer.text(
                    ClipMode.values()[n].name,
                    Vector2(20.0, 20.0 + n * 100.0)
                )
            }
        }
    }
}
