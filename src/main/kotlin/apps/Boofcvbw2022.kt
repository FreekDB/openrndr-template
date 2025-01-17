package apps

import aBeLibs.extensions.NoJitter
import aBeLibs.geometry.pillShape
import aBeLibs.geometry.smoothed
import aBeLibs.geometry.toContours
import aBeLibs.gui.GUI
import aBeLibs.lang.doubleRepeat
import aBeLibs.lang.loopRepeat
import aBeLibs.math.map
import aBeLibs.random.rnd
import aBeLibs.svg.Pattern
import aBeLibs.svg.fill
import com.soywiz.korma.random.randomWithWeights
import org.openrndr.*
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.draw.LineCap
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.fx.blur.ApproximateGaussianBlur
import org.openrndr.extra.fx.distort.Perturb
import org.openrndr.extra.noise.Random
import org.openrndr.extra.noise.random
import org.openrndr.extra.noise.uniform
import org.openrndr.extra.parameters.ActionParameter
import org.openrndr.extra.parameters.Description
import org.openrndr.extra.parameters.IntParameter
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.shape.*
import org.openrndr.svg.saveToFile
import java.io.File
import kotlin.math.PI
import kotlin.math.min

/**
 * id: 9231f41d-6600-4c20-b3bc-84c0d8a48922
 * description: Creates black and white designs. Applies glitchy effect and blur.
 * Then uses BoofCV to trace contours. Finally, fills contours with patterns.
 * tags: #axi
 */

fun main() = application {
    configure {
        width = 1800
        height = 1800
    }
    program {
        val gui = GUI("F7D431", "F72F57", "C110C8", "0A7AAD", "0A7AAD").apply {
            compartmentsCollapsedByDefault = false
        }

        val bw = renderTarget(width, height) {
            colorBuffer()
            depthBuffer()
        }
        val bwBlurred = colorBuffer(width, height)
        val withFX = colorBuffer(width, height)
        val blur = ApproximateGaussianBlur().apply {
            window = 4
            sigma = 4.0
        }
        val fx = Perturb()
        val screenshots = Screenshots()
        val svg = drawComposition { }
        val patterns = drawComposition { }
        val shape = mutableListOf<Shape>()
        val colors = listOf(ColorRGBa.BLACK, ColorRGBa.WHITE)

        Random.seed = System.currentTimeMillis().toString()

        val params = @Description("Params") object {
            @IntParameter("segmentCount", 5, 30)
            var segmentCount = 10
        }

        /**
         * Adds parallel or perpendicular lines to a "pill"
         */
        fun Drawer.parallels(t: List<Pair<Vector2, Vector2>>) {
            val s = if (Random.bool()) {
                // parallel
                val a = Random.double(0.3, 0.7)
                val b = Random.double(0.3, 0.7)
                listOf(
                    LineSegment(t[0].first, t[0].second).sub(a, b),
                    LineSegment(t[1].first, t[1].second).sub(a, b)
                )
            } else {
                // perpendicular
                val lim = Random.double(0.1, 0.4)
                listOf(
                    LineSegment(t[0].first, t[1].first).sub(0.0, lim),
                    LineSegment(t[0].second, t[1].second).sub(0.0, lim)
                )
            }
            stroke = colors[0]
            strokeWeight = 6.0
            val rep = min(
                s[0].start.distanceTo(s[1].start),
                s[0].end.distanceTo(s[1].end)
            ).toInt() / Random.int(10, 40)
            doubleRepeat(rep, 0.2, 0.8) {
                lineSegment(s[0] * (1 - it) + s[1] * it)
            }
        }

        /**
         * Adds inner or outer circles to an existing circle
         */
        fun Drawer.radials(c: Circle) {
            val sz = Random.double(4.0, 20.0)
            val radius = c.radius * Random.double(0.5, 1.5)
            val color = Random.int0(2)
            val rep = (c.radius * 2 * PI).toInt() / Random.int(20, 40)
            stroke = colors[color]
            fill = colors[1 - color]
            strokeWeight = if (Random.bool()) 6.0 else 0.0
            loopRepeat(rep, 0.0, 360.0) {
                circle(c.center + Polar(it, radius).cartesian, sz)
            }
        }

        /**
         * Populate [segments] (the skeleton of the shape)
         */
        fun genSpine(): List<Segment> {
            val spine = mutableListOf<Segment>()
            val angles = listOf(
                List(4) { 15.0 + it * 90.0 },
                List(5) { 15.0 + it * 72.0 },
                List(6) { 15.0 + it * 60.0 }
            ).random()
            val lengths = listOf(125.0, 250.0)
            val okArea = drawer.bounds.offsetEdges(-100.0)
            val center = okArea.position(0.5, 0.5)
            fun offsetFrom(start: Vector2) = (start + Polar(
                angles.random(), lengths.random()
            ).cartesian).clamp(okArea)
            while (spine.size < params.segmentCount) {
                if (spine.isEmpty()) {
                    spine.add(Segment(center, offsetFrom(center)))
                } else {
                    // A random end-point in a spine segment, some segments more likely
                    val start = spine
                        .randomWithWeights(List(spine.size) { i -> 1.0 + i * i })
                        .position(listOf(0.0, 1.0).random())
                    val end = offsetFrom(start)
                    val segNew = Segment(start, end)

                    // Add if not intersecting or too close
                    if (spine.all { other ->
                            other.nearest(end).position.distanceTo(end) > 80.0 &&
                                    other.intersections(segNew).none {
                                        it.b.segmentT > 0
                                    } // center = 500, x = 600, center * 2 - x = 400
                        }) {
                        spine.add(segNew)

                        // mirrored
                        spine.add(
                            Segment(
                                center * 2.0 - start,
                                center * 2.0 - end
                                //start.copy(x = center.x * 2 - start.x),
                                //end.copy(x = center.x * 2 - end.x)
                            )
                        )
                    }
                }
            }
            return spine.distinctBy { it.start.toInt() + it.end.toInt() }
        }

        fun newImage2(spine: List<Segment>) {
            drawer.isolatedWithTarget(bw) {
                clear(colors.first())
                stroke = null

                // Circles
                spine.forEachIndexed { i, it ->
                    fill = colors[i % 2]
                    stroke = colors[(i + 1) % 2]
                    strokeWeight = random(8.0, 24.0)
                    val pc = 1 - i.toDouble() / (spine.size - 1)
                    val r = (1 - pc * pc).map(200.0, 20.0)
                    circle(it.start, r)
                    strokeWeight = random(8.0, 24.0)
                    circle(it.end, r * Random.double(0.9, 1.1))
                }

                // Pills
                spine.forEachIndexed { i, it ->
                    fill = colors[(i + 1) % 2]
                    stroke = null
                    val c0 = Circle(it.start, Random.int(1, 5) * 20.0)
                    val c1 = Circle(it.end, Random.int(1, 5) * 20.0)
                    contour(pillShape(c0, c1))

                    fill = colors[i % 2]
                    contour(
                        pillShape(
                            c0.copy(radius = c0.radius * 0.9),
                            c1.copy(radius = c1.radius * 0.9)
                        )
                    )

                    when (Random.double0()) {
                        in 0.00..0.33 -> parallels(c0.tangents(c1))
                        in 0.33..0.66 -> radials(if (Random.bool()) c0 else c1)
                    }

                }


                // Small axis circles
                spine.forEachIndexed { i, it ->
                    fill = colors[(i + 1) % 2]
                    circle(it.start, Random.int(1, 5) * 10.0)
                    circle(it.end, Random.int(1, 5) * 10.0)
                }

                // Scratches
                lineCap = LineCap.ROUND
                repeat(100) {
                    strokeWeight = 6.0 rnd 20.0
                    stroke = colors.random().opacify(0.3)
                    val start = bounds.uniform(100.0)
                    val end =
                        start + Polar(Random.double0(360.0), 100.0).cartesian
                    val c0 =
                        start + Polar(Random.double0(360.0), 50.0).cartesian
                    val c1 = end + Polar(Random.double0(360.0), 50.0).cartesian
                    segment(Segment(start, c0, c1, end))
                }
            }
        }

        fun imageToShape() {
            fx.apply(bw.colorBuffer(0), withFX)
            blur.apply(withFX, bwBlurred)
            shape.add(Shape(bwBlurred.toContours(0.5).map {
                it.smoothed(2)
            }))
        }

        fun shapeToSVG() {
            svg.draw {
                fill = null
                stroke = ColorRGBa.BLACK
                shapes(shape)
            }
        }

        val actions = @Description("Actions") object {
            @ActionParameter("A1. new image", 1)
            fun doNew() {
                Random.seed = System.currentTimeMillis().toString()
                newImage2(genSpine())
            }

            @ActionParameter("A2. to curves", 2)
            fun doCreateContours() {
                svg.clear()
                shape.clear()
                imageToShape()
                shapeToSVG()
            }

            @ActionParameter("A3. pattern fill", 3)
            fun doFill() {
                Pattern.stroke = true
                patterns.clear()
                shape.forEachIndexed { i, shp ->
                    patterns.fill(
                        shp, when (i % 3) {
                            0 -> Pattern.NOISE(
                                2.0,
                                1.0,
                                180.0,
                                20.0 rnd 30.0,
                                0.006 rnd 0.009
                            )
                            1 -> Pattern.NOISE(
                                2.0,
                                0.6,
                                90.0,
                                20.0 rnd 30.0,
                                0.010 rnd 0.015
                            )
                            2 -> Pattern.NOISE(
                                1.8,
                                1.1,
                                0.0,
                                10.0 rnd 20.0,
                                0.003 rnd 0.006
                            )
                            else -> Pattern.HAIR(
                                5.0,
                                0.001,
                                8.0
                            )
                        }
                    )
                }
            }

            @ActionParameter("B. multilayer shape", 4)
            fun doMultilayer() {
                svg.clear()
                shape.clear()
                patterns.clear()
                val spine = genSpine()
                repeat(3) {
                    Random.seed = (frameCount + it).toString()
                    newImage2(spine)
                    imageToShape()
                }

                // shape 0 without 1 and 2 (occluded by them)
                val shape0 = Shape.compound(drawComposition {
                    shape(shape[0])
                    clipMode = ClipMode.DIFFERENCE
                    shape(shape[1])
                    shape(shape[2])
                }.findShapes().map { it.shape })

                // shape 1 without 2 (occluded by it
                val shape1 = Shape.compound(drawComposition {
                    shape(shape[1])
                    clipMode = ClipMode.DIFFERENCE
                    shape(shape[2])
                }.findShapes().map { it.shape })

                shape[0] = shape0
                shape[1] = shape1

                shapeToSVG()
            }

            @ActionParameter("screenshot", 5)
            fun doScreenshot() = screenshots.trigger()

            @ActionParameter("save svg", 6)
            fun doSaveSVG() {
                val result = drawComposition {
                    composition(svg)
                    composition(patterns)
                }
                result.saveToFile(
                    File(program.namedTimestamp("svg", "print"))
                )
            }

        }

        extend(gui) {
            add(actions)
            add(params)
            add(blur)
            add(fx)
        }
        extend(screenshots)
        extend(NoJitter())
        extend {
            drawer.isolated {
                clear(ColorRGBa.WHITE)
                if (keyboard.pressedKeys.contains("left-shift")) {
                    image(bw.colorBuffer(0))
                } else {
                    composition(svg)
                    composition(patterns)
                }
            }
        }
        gui.loadParameters(File("data/parameters/Boofcvbw001.json"))
    }
}

