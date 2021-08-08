package latest


import org.openrndr.color.ColorRGBa
import org.openrndr.color.hsl
import org.openrndr.draw.isolated
import org.openrndr.extra.noise.uniform
import org.openrndr.math.Vector2
import org.openrndr.shape.Circle

fun main() = applicationSynchronous {
    configure {
        width = 1200
        height = 800
    }
    program {
        val b = drawer.bounds.offsetEdges(-200.0)
        val contours = List(10) {
            Circle(Vector2.uniform(b), 200.0).contour
        }
        extend {
            drawer.clear(ColorRGBa.WHITE)
            contours.forEach { c ->
                drawer.isolated {
                    fill = ColorRGBa.BLACK.opacify(0.1)
                    stroke = ColorRGBa.BLACK.opacify(0.2)
                    contour(c)

                    val parts = c.split(contours)
                    fill = ColorRGBa.BLACK.opacify(0.5)
                    text("${parts.size} parts", c.bounds.center)
                    fill = null
                    strokeWeight = 4.0
                    parts.forEachIndexed { i, it ->
                        stroke = hsl(i * 200.0, 1.0, 0.5).toRGBa()
                        contour(it)
                        circle(it.position(0.0), 5.0 + i % 7.0)
                        circle(it.position(1.0), 15.0 + i % 7.0)
                    }
                }
            }
        }
    }
}
