package axi

import aBeLibs.geometry.bend
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.dialogs.saveFileDialog
import org.openrndr.extra.noise.Random
import org.openrndr.math.Vector2
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.contour
import org.openrndr.shape.drawComposition
import org.openrndr.shape.split
import org.openrndr.svg.loadSVG
import org.openrndr.svg.writeSVG

/**
 * Click and drag to make a cut on an SVG.
 * The actual knife shape uses mouse drag start and end plus two randomized control points, to create a curve.
 * The SVG is hardcoded and loaded on start.
 * After creating the cut the program asks where to save the modified SVG.
 */
fun main() = application {
    configure {
        width = 1920
        height = 1080
    }
    program {
        val shapes = loadSVG(
            "/home/funpro/OR/openrndr-template/print/2021-01-02-brit6.svg"
        ).findShapes().map { it.shape }

        var mouseStart = Vector2.ZERO
        var mouseEnd: Vector2
        val offset = Vector2(500.0)
        var knife = ShapeContour.EMPTY

        extend {
            drawer.translate(-offset)
            drawer.fill = null
            drawer.stroke = ColorRGBa.WHITE
            drawer.shapes(shapes)
            drawer.stroke = ColorRGBa.RED
            drawer.contour(knife)
        }

        mouse.buttonDown.listen {
            mouseStart = mouse.position + offset
        }
        mouse.buttonUp.listen {
            mouseEnd = mouse.position + offset
            knife = contour {
                moveTo(mouseStart)
                curveTo(
                    mouseStart.mix(mouseEnd, 0.33) + Random.vector2(-100.0, 100.0),
                    mouseStart.mix(mouseEnd, 0.66) + Random.vector2(-100.0, 100.0),
                    mouseEnd
                )
            }.sampleEquidistant(50)
            val cutLines = shapes.flatMap {
                it.contours
            }.flatMap { split(it, knife) }.bend(knife)

            val svg = drawComposition {
                fill = null
                stroke = ColorRGBa.BLACK
                contours(cutLines)
            }
            saveFileDialog(supportedExtensions = listOf("svg")) {
                it.writeText(writeSVG(svg))
            }
        }

    }
}

