package axi

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.LineJoin
import org.openrndr.extensions.Screenshots
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import org.openrndr.shape.contour
import org.openrndr.shape.draw
import org.openrndr.shape.drawComposition

/**
 * Test. SVG not showing contours for some reason
 */

fun main() {
    application {
        program {
            val svg = drawComposition { }

            fun newDesign() {
                svg.clear()
                svg.draw {
                    fill = null
                    stroke = ColorRGBa.BLACK
                    contour(Rectangle(Vector2.ZERO, 200.0).contour)
//                    contour(contour {
//                        moveTo(Vector2.ZERO)
//                        lineTo(drawer.bounds.dimensions)
//                    })
                }
            }

            newDesign()

            extend(Screenshots())
            extend {
                drawer.clear(ColorRGBa.PINK)
                drawer.lineJoin = LineJoin.ROUND
                drawer.composition(svg)
            }

            mouse.buttonDown.listen {
                newDesign()
            }
        }
    }
}
