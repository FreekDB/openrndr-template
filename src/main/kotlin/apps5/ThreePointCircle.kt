package apps5

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.loadFont
import org.openrndr.extra.noise.Random
import org.openrndr.shape.Circle

fun main() = application {
    configure {
        width = 800
        height = 600
        title = "Circumcircle"
    }
    program {
        val font = loadFont("data/fonts/IBMPlexMono-Regular.ttf", 16.0)
        val points = MutableList(3) { Random.point(drawer.bounds) }
        var c = Circle.fromPoints(points[0], points[1], points[2])

        extend {
            drawer.fill = null
            drawer.stroke = ColorRGBa.WHITE
            drawer.circle(c)

            drawer.stroke = ColorRGBa.RED
            drawer.lineLoop(points)

            drawer.fill = ColorRGBa.WHITE
            drawer.fontMap = font
            drawer.texts(points.map { it.toInt().toString() }, points)

            drawer.fill = ColorRGBa.YELLOW
            drawer.stroke = null
            drawer.circles(points, 5.0)
        }

        mouse.buttonDown.listen {
            points.removeAt(0)
            points.add(it.position)
            c = Circle.fromPoints(points[0], points[1], points[2])
        }
    }
}