package axi

import aBeLibs.extensions.Handwritten
import aBeLibs.geometry.circleish
import aBeLibs.random.rnd
import aBeLibs.svg.Pattern
import aBeLibs.svg.fill
import org.openrndr.*
import org.openrndr.color.ColorRGBa
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.Random
import org.openrndr.extra.noise.uniform
import org.openrndr.extra.shapes.grid
import org.openrndr.math.Vector2
import org.openrndr.shape.Circle
import org.openrndr.shape.CompositionDrawer
import org.openrndr.shape.Shape
import org.openrndr.shape.draw
import org.openrndr.svg.saveToFile
import java.io.File

/**
 * Creates a number of shapes each filled with a different pattern.
 * The shapes are vertically centered and separated from each other.
 */
fun main() = application {
    configure {
        width = 1200
        height = 600
    }
    program {
        val handwritten = Handwritten().also {
            it.scale = 1.5
        }
        extend(handwritten)

        Random.seed = System.currentTimeMillis().toString()
        val patterns = listOf(
            Pattern.STRIPES(
                1.0 rnd 5.5,
                0.5 rnd 1.0,
                0.0 rnd 360.0
            ),
            Pattern.HAIR(
                2.0 rnd 10.0,
                0.0005 rnd 0.005,
                4.0 rnd 10.0
            ),
            Pattern.PERP(
                2.0 rnd 10.0,
                4.0 rnd 10.0
            ),
            Pattern.DOTS(
                0.01,
                3.0, 10.0,
                5.0, 3.0
            ),
            Pattern.CIRCLES(
                0.2 rnd 2.0,
                Vector2.uniform(-Vector2.ONE) * 50.0,
                0.7
            ),
            Pattern.NOISE(
                1.0,
                0.4, // 0.8
                0.0 rnd 360.0,
                20.0,
                0.008
            )
        )

        val svg = drawComposition { }
        val cells = drawer.bounds
            .grid(patterns.size, 3, 50.0, 50.0)

        fun newDesign() {
            Random.seed = System.currentTimeMillis().toString()

            svg.clear()
            Pattern.stroke = false
            cells.forEachIndexed { y, rects ->
                rects.forEachIndexed { i, rect ->
                    val outline = when (y) {
                        0 -> rect.offsetEdges(-20.0).shape
                        1 -> Circle(rect.center, rect.width * 0.4).shape
                        else -> Shape(
                            listOf(
                                circleish(rect.center, rect.width * 0.4),
                                circleish(
                                    rect.center,
                                    rect.width * 0.2
                                ).reversed
                            )
                        )
                    }
                    svg.fill(outline, patterns[i])
                }
            }

            // Text
            handwritten.clear()
            patterns.forEachIndexed { i, p ->
                handwritten.add(
                    p.toString().substringBefore("("),
                    cells[2][i].position(0.3, 1.1)
                )
            }
            val text = CompositionDrawer().also {
                it.fill = null
                it.stroke = ColorRGBa.BLACK
            }
            handwritten.drawToSVG(text)
            svg.draw {
                composition(text.composition)
            }
        }

        newDesign()

        extend(Screenshots())
        extend {
            drawer.clear(ColorRGBa.PINK)
            drawer.composition(svg)
        }

        mouse.buttonDown.listen {
            newDesign()
        }

        keyboard.keyDown.listen {
            when (it.key) {
                KEY_ENTER -> svg.saveToFile(
                    File(program.namedTimestamp("svg", "print"))
                )
                KEY_ESCAPE -> application.exit()
            }
        }

    }
}
