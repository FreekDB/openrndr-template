package apps5

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extensions.Screenshots
import org.openrndr.shape.Circle

/**
 * id: 896f0ca3-d09d-47d3-a34a-d9cc1b98efdf
 * description: Test antialias of Circle and ShapeContour
 * tags: #new
 */

fun main() = application {
    program {
        fun draw(u: Double) {
            val pTop = drawer.bounds.position(u, 0.33)
            val pBottom = drawer.bounds.position(u, 0.66)
            drawer.circle(pTop, 70.0)
            drawer.contour(Circle(pBottom, 70.0).contour)
        }
        println(
            """
            circle:  stroke=WHITE | strokeWeight=0 | stroke=null
            contour: stroke=WHITE | strokeWeight=0 | stroke=null
        """.trimIndent()
        )

        extend(Screenshots())
        extend {
            drawer.stroke = ColorRGBa.WHITE
            draw(0.25)

            drawer.strokeWeight = 0.0
            draw(0.50)

            drawer.stroke = null
            draw(0.75)
        }
    }
}
