package axi

import aBeLibs.extensions.TransRotScale
import aBeLibs.geometry.circleish2
import aBeLibs.geometry.makeParallelCurve
import aBeLibs.geometry.symmetrizeSimple
import org.openrndr.*
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.LineCap
import org.openrndr.draw.LineJoin
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.gui.GUI
import org.openrndr.extra.noise.Random
import org.openrndr.extra.parameters.Description
import org.openrndr.extra.parameters.DoubleParameter
import org.openrndr.extra.parameters.IntParameter
import org.openrndr.extras.color.presets.WHEAT
import org.openrndr.math.Polar
import org.openrndr.shape.*
import org.openrndr.svg.saveToFile
import java.io.File
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min

/**
 * id: 1e1a1305-5d84-4fc5-8f50-6e869353f532
 * description: New sketch
 * tags: #new
 */

fun main() = application {
    configure {
        fullscreen = Fullscreen.CURRENT_DISPLAY_MODE
    }
    program {
        val svg = drawComposition { }
        val gui = GUI().apply {
            compartmentsCollapsedByDefault = false
        }

        val core = @Description("Core") object {
            @IntParameter("Num children", 1, 10, order = 0)
            var numChildren = 3

            @DoubleParameter("Noise amount", 0.001, 0.1, order = 1)
            var noiseScale = 0.02

            @DoubleParameter("Noise detail", 0.5, 2.0, order = 2)
            var noiseFreq = 1.2
        }

        val petal = @Description("Petal") object {
            @DoubleParameter("Noise amount", 0.001, 0.1, order = 1)
            var noiseScale = 0.02

            @DoubleParameter("Noise detail", 0.5, 2.0, order = 2)
            var noiseFreq = 1.2

            @DoubleParameter("Min size", 0.2, 0.8, order = 3)
            var minSize = 0.5

            @DoubleParameter("Max size", 0.2, 0.8, order = 4)
            var maxSize = 0.5

            @DoubleParameter("Min offset", 0.5, 2.0, order = 5)
            var minOffset = 1.3

            @DoubleParameter("Max offset", 0.5, 2.0, order = 6)
            var maxOffset = 1.3
        }

        val children = @Description("Children") object {
            @DoubleParameter("Size randomness", 0.0, 1.0, order = 1)
            var sizeRandomness = 0.03

            @DoubleParameter("Scaling", 0.1, 1.0, order = 2)
            var scale = 0.5

            @DoubleParameter("Noise add", 0.000, 0.5, order = 3)
            var noiseAdd = 0.02

            @DoubleParameter("Noise mult", 0.001, 0.1, order = 4)
            var noiseScale = 0.02

            @DoubleParameter("Noise detail", 0.5, 2.0, order = 5)
            var noiseFreq = 1.2
        }

        fun newDesign() {
            svg.clear()

            Random.seed = System.currentTimeMillis().toString()

            val shapes = mutableListOf<ShapeContour>()
            val siblings = mutableListOf<ShapeContour>()
            val ints = mutableListOf<ContourIntersection>()

            val radius = height * 0.4
            val pointCount = radius.toInt() / 2

            fun addShape(shp: ShapeContour, copies: Int) {
                shapes.forEach {
                    ints.addAll(shp.intersections(it))
                }
                shapes.add(shp)
                siblings.addAll(shp.copies(copies))
            }


            // A. core
            addShape(
                circleish2(
                    drawer.bounds.center, radius, pointCount, 0.0,
                    core.noiseScale, core.noiseFreq
                ), 5
            )

            // B. petals
            repeat(core.numChildren) {
                val theta = it * (360.0 / core.numChildren) - 90
                val offset = radius * Random.double(
                    petal.minOffset,
                    petal.maxOffset
                )
                val pos = drawer.bounds.center + Polar(theta, offset).cartesian
                val rad = radius * Random.double(
                    petal.minSize,
                    petal.maxSize
                )
                addShape(
                    circleish2(
                        pos, rad, pointCount, theta,
                        petal.noiseScale, petal.noiseFreq
                    ), 4
                )
            }

            // C. intersections
            val iterations = 4
            repeat(iterations) {
                val intsIt = ints.listIterator()
                while (intsIt.hasNext()) {
                    val intersection = intsIt.next()
                    intsIt.remove()

                    // calculate new radius
                    val sizeA = intersection.a.contour.bounds.width
                    val sizeB = intersection.b.contour.bounds.width
                    val nuRadius = min(sizeA, sizeB) *
                            Random.double(
                                1 - children.sizeRandomness,
                                1 + children.sizeRandomness
                            ) * children.scale

                    val nuPointCount = nuRadius.toInt() * 2
                    if (nuPointCount > 3) {
                        val nu = circleish2(
                            intersection.position,
                            nuRadius,
                            nuPointCount,
                            0.0,
                            children.noiseAdd + it * children.noiseScale,
                            children.noiseFreq
                        )
                        val notNested = shapes.none { other ->
                            nu.contains(other.bounds.center) &&
                                    other.contains(intersection.position)
                        }
                        if (notNested) {
                            shapes.forEach { previous ->
                                if (nu.bounds.intersects(previous.bounds)) {
                                    nu.intersections(previous)
                                        .forEach { nuInt ->
                                            intsIt.add(nuInt)
                                        }
                                }
                            }
                            shapes.add(nu)
                            siblings.addAll(nu.copies(iterations - 1 - it))
                        }
                    }
                }
            }

            svg.draw {
                fill = null
                stroke = ColorRGBa.BLACK.opacify(0.7)
                contours(shapes)
                contours(siblings)
            }

        }
        newDesign()


        extend(gui) {
            add(core)
            add(petal)
            add(children)
        }
        extend(Screenshots())
        extend(TransRotScale())
        extend {
            drawer.clear(ColorRGBa.WHEAT)
            drawer.lineJoin = LineJoin.BEVEL
            drawer.lineCap = LineCap.ROUND
            drawer.composition(svg)
        }

        keyboard.keyDown.listen {
            when (it.key) {
                KEY_ENTER -> svg.saveToFile(
                    File(
                        program.namedTimestamp("svg", "print")
                    )
                )
                KEY_ESCAPE -> application.exit()
                else -> when (it.name) {
                    "n" -> newDesign()
                }
            }
        }
    }
}

/**
 * Creates wobbly offset clones of a [ShapeContour]
 */
private fun ShapeContour.copies(count: Int) = List(count) { lineNum ->
    val len = length.toInt()
    val side = (lineNum % 2) * 2 - 1
    val offset = lineNum / 2 + 1
    ShapeContour.fromPoints(
        List(len) {
            position(it / len.toDouble())
        }, closed
    ).makeParallelCurve { pc ->
        (side * offset) * (1.0 + 0.5 * cos(pc * PI * 2 * 3 + lineNum * 0.2))
    }.symmetrizeSimple()
}
