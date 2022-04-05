package axi

import aBeLibs.geometry.spiralContour
import aBeLibs.svg.saveToInkscapeFile
import aBeLibs.svg.setInkscapeLayer
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.dialogs.saveFileDialog
import org.openrndr.drawComposition
import org.openrndr.extra.color.spaces.ColorHSLUVa
import org.openrndr.extra.noise.Random
import org.openrndr.extra.shapes.grid
import org.openrndr.math.Vector2
import org.openrndr.namedTimestamp
import org.openrndr.shape.Circle
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.draw
import kotlin.math.min

/**
 * id: f326736e-3c6c-4028-9fb0-9a5481b2f741
 * description: Single line filled rings for plotting
 * tags: #ring #axi
 */


fun main() = application {
    program {
        fun filledDonut(
            center: Vector2,
            innerRadius: Double,
            outerRadius: Double,
            separation: Double = 5.0
        ): ShapeContour {
            val turns = ((outerRadius - innerRadius) / separation).toInt()
            val start = Circle(center, outerRadius).contour.open
            val end = Circle(center, innerRadius).contour.open
            val mid = spiralContour(end.position(0.0), start.position(1.0), center, turns)
            return start + mid + end
        }

        val maxRadius = 70.0
        val locations = drawer.bounds.grid(4, 3).flatten()
        val numColors = 6
        val svg = drawComposition { }
        val minRadius = 20.0
        val bandRadius = 10.0
        val bandSep = 4.0
        val donutCollections = List(numColors) { mutableListOf<ShapeContour>() }

        // TODO: Implement layers
        var i = 0
        locations.forEach {
            var radius = minRadius
            while (radius < maxRadius) {
                val radiusInc = Random.int(1, 3) * bandRadius
                i = (i + Random.int(1, numColors)) % numColors
                donutCollections[i].add(
                    filledDonut(
                        it.center, radius,
                        min(radius + radiusInc - bandSep, maxRadius), 2.0
                    )
                )
                radius += radiusInc
            }
        }

        svg.draw {
            donutCollections.forEachIndexed { i, donuts ->
                val layerName = "${if (i > 0) "!" else ""}+S70 layer$i"

                group {
                    fill = null
                    stroke = ColorHSLUVa(60.0 * i, 0.8, 0.6).toRGBa()
                    contours(donuts)
                }.setInkscapeLayer(layerName)
            }
        }

        extend {
            drawer.run {
                clear(ColorRGBa.WHITE)
                composition(svg)
            }
        }

        keyboard.keyDown.listen {
            if (keyboard.pressedKeys.contains("left-control")) {
                when (it.name) {
                    "s" -> {
                        val name = program.namedTimestamp("svg", "print")
                        println(name)
                        saveFileDialog(
                            suggestedFilename = name,
                            supportedExtensions = listOf("svg")
                        ) { file ->
                            svg.saveToInkscapeFile(file)
                        }
                    }
                }
            }
        }
    }
}
