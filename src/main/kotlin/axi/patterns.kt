package axi

import aBeLibs.random.pickWeighted
import aBeLibs.random.rnd
import aBeLibs.svg.Pattern
import aBeLibs.svg.fill
import org.openrndr.KEY_ENTER
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.Random
import org.openrndr.extra.noise.uniform
import org.openrndr.math.CatmullRomChain2
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.namedTimestamp
import org.openrndr.shape.*
import org.openrndr.svg.saveToFile
import java.io.File

/**
 * id: 76cd1349-1ea1-4d89-b7a4-b904195fd3b2
 * description: New sketch
 * tags: #new
 */

fun main() = application {
    fun circleish(pos: Vector2, radius: Double): ShapeContour =
        CatmullRomChain2(List(5) { it * 72.0 + Random.double0(30.0) }.map {
            Polar(it, radius).cartesian + pos
        }, 0.5, true).toContour()

    configure {
        width = 1500
        height = 1000
    }
    program {
        Random.seed = System.currentTimeMillis().toString()

        val svg = drawComposition { }
        // to avoid doing clipping between A and B both ways (creating a
        // hole)
        val usedPairs = mutableSetOf<Set<Shape>>()

        // TODO:
        // Make photo based pattern

        fun newDesign() {
            svg.root.findAll { it != svg.root }
                .forEach { it.remove() }

            // 1. make a list of shapes
            //val target = drawer.bounds.offsetEdges(-200.0)
            val shapes = MutableList(15) {
                // Polar layout
                val type =
                    listOf(0, 1, 2).pickWeighted(listOf(0.1, 0.3, 0.6))
                val angles = (type + 1) * 3
                val pos = Polar(
                    Random.int0(angles) * (360.0 / angles),
                    listOf(0.0, 250.0, 350.0)[type]
                ).cartesian
                val sz = 200.0 - 80.0 * type

                // Grid layout
                //val grid = Rectangle(Vector2.ZERO, 6.0, 5.0)
                //val pos = grid.randomPoint().round(0).map(
                //    grid, target) - drawer.bounds.center
                //val sz = Random.double0() * Random.double0() * 150.0 + 40.0

                // Make it XY Mirrored
                val szDiff = 0.0 rnd 20.0
                Pair(
                    circleish(pos, sz + szDiff).shape,
                    circleish(-pos, sz - szDiff).shape
                )
            }.map { it.toList() }.flatten()

            // 2. test all pairs for overlap.
            // 3. if overlap and 50% chance, replace one of the shapes
            //    by a cut version of it (difference)
            val shapesCropped = shapes.map { shapeA ->
                if (Random.bool(0.9)) {
                    val cutterShape = shapes.firstOrNull { shapeB ->
                        shapeA != shapeB
                                &&
                                !usedPairs.contains(setOf(shapeA, shapeB))
                                && (
                                shapeA.contains(shapeB.bounds.center)
                                        ||
                                        shapeB.contains(shapeA.bounds.center)
                                        ||
                                        shapeA.intersections(shapeB)
                                            .isNotEmpty()
                                )

                    }
                    cutterShape?.let { cutter ->
                        usedPairs.add(setOf(shapeA, cutter))
                        val co = compound {
                            difference {
                                shape(shapeA)
                                shape(cutter)
                            }
                        }
                        if (co.isNotEmpty()) {
                            return@map co.first()
                        }
                    }
                }
                shapeA
            }

            // 4. iterate over all shapes and do svg.draw(pattern)
            Pattern.stroke = false
            shapesCropped.forEach { outline ->
                svg.fill(
                    outline, when (0 rnd 5) {
                        0 -> Pattern.STRIPES(
                            1.0 rnd 5.5,
                            0.5 rnd 1.0,
                            0.0 rnd 360.0
                        )
                        1 -> Pattern.HAIR(
                            2.0 rnd 10.0,
                            0.0005 rnd 0.005,
                            4.0 rnd 10.0
                        )
                        2 -> Pattern.PERP(
                            2.0 rnd 10.0,
                            4.0 rnd 10.0
                        )
                        3 -> Pattern.DOTS(
                            0.001,
                            3.0, 15.0,
                            5.0, 3.0
                        )
                        else -> Pattern.CIRCLES(
                            0.2 rnd 2.0,
                            Vector2.uniform(-Vector2.ONE) * 50.0,
                            0.1 rnd 0.5
                        )
                    }
                )
            }
        }

        newDesign()

        extend(Screenshots())
        extend {
            drawer.clear(ColorRGBa.PINK)
            drawer.translate(drawer.bounds.center)
            drawer.composition(svg)
        }

        mouse.buttonDown.listen {
            newDesign()
        }

        keyboard.keyDown.listen {
            when (it.key) {
                KEY_ENTER -> svg.saveToFile(
                    File(
                        program.namedTimestamp("svg", "print")
                    )
                )
            }
        }

    }
}
