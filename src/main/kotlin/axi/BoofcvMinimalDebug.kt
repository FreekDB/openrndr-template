package axi


import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.LineJoin
import org.openrndr.extensions.Screenshots
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import org.openrndr.shape.draw
import org.openrndr.shape.drawComposition

/**
 * id: a7bbfc65-240a-41e7-a845-795ee1932f5b
 * description: SVG not showing contours for some reason
 * tags: #test
 */

fun main() =
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
