package apps2.simpleTests

import aBeLibs.extensions.TransRotScale
import aBeLibs.geometry.symmetrizeSimple

import org.openrndr.color.ColorRGBa
import org.openrndr.color.rgb
import org.openrndr.shape.Circle

fun main() = applicationSynchronous {
    configure {
        width = 800
        height = 800
    }
    program {
        val circle = Circle(drawer.bounds.center, 300.0).contour
        val ugly = circle.offset(-125.0)
        // Note: here I was using .beautify()
        // but .symmetrizeSimple() works better
        val nice = ugly.symmetrizeSimple()
        extend(TransRotScale())
        extend {
            drawer.apply {
                clear(rgb(0.7))
                fill = null

                strokeWeight = 0.3
                stroke = ColorRGBa.BLUE
                contour(circle)

                strokeWeight = 3.0
                stroke = ColorRGBa.RED
                contour(ugly)

                strokeWeight = 1.0
                stroke = ColorRGBa.WHITE
                contour(nice)

                strokeWeight = 0.3
                stroke = ColorRGBa.BLACK.opacify(0.5)
                circles(nice.segments.map { it.start }, 1.0)
            }
        }
    }
}
