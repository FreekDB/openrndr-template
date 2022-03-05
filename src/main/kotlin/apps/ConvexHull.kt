package apps

import aBeLibs.geometry.convexHull
import org.openrndr.KEY_ENTER
import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.uniformRing
import org.openrndr.math.Vector2
import org.openrndr.shape.ShapeContour
import kotlin.system.exitProcess

/**
 * id: b4599fcc-9a24-4224-b3f8-a3f2a72a866a
 * description: New sketch
 * tags: #new
 */


@ExperimentalStdlibApi
fun main() = application {
    configure {
        width = 500
        height = 500
    }

    lateinit var points: ShapeContour
    var hull = listOf<Vector2>()

    fun populate() {
        points = ShapeContour.fromPoints(List(20) {
            Vector2.uniformRing(20.0, 200.0)
        }, true)

        hull = convexHull(points.segments.map { it.start })
        println(hull.size)
    }

    populate()

    program {
        extend(Screenshots())
        extend {
            drawer.run {
                clear(ColorRGBa.WHITE)
                translate(bounds.center)

                stroke = null
                fill = ColorRGBa.PINK
                contour(points)

                fill = ColorRGBa.BLACK
                circles(points.segments.map { it.start }, 4.0)

                strokeWeight = 4.0
                stroke = ColorRGBa.BLUE.opacify(0.7)
                lineLoop(hull)

                strokeWeight = 2.0
                stroke = ColorRGBa.GREEN
                lineStrip(hull)

                stroke = ColorRGBa.BLACK
                fill = null
                circles(hull, 8.0)
            }
        }
        keyboard.keyDown.listen {
            when (it.key) {
                KEY_ESCAPE -> exitProcess(0)
                KEY_ENTER -> populate()
            }
        }
    }
}
