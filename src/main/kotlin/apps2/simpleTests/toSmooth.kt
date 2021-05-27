package apps2.simpleTests

import aBeLibs.extensions.TransRotScale
import aBeLibs.geometry.beautify
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.color.rgb
import org.openrndr.svg.loadSVG

fun main() = application {
    configure {
        width = 800
        height = 800
    }
    program {
        val svg = loadSVG("print/to-smooth.svg")
        val ugly = svg.findShapes().first().shape.contours.first()
        val nice = ugly.beautify()
        extend(TransRotScale())
        extend {
            drawer.clear(rgb(0.7))
            drawer.fill = null
            drawer.strokeWeight = 0.3

            drawer.stroke = ColorRGBa.BLACK.opacify(0.2)
            drawer.contour(ugly)

            drawer.stroke = ColorRGBa.WHITE
            drawer.contour(nice)

            drawer.strokeWeight = 0.3
            drawer.circles(nice.segments.map { it.start }, 1.0)
        }
    }
}
