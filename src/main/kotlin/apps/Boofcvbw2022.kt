package apps

import aBeLibs.extensions.NoJitter
import aBeLibs.geometry.pillShape
import aBeLibs.geometry.smoothed
import aBeLibs.geometry.toContours
import aBeLibs.lang.doubleRepeat
import aBeLibs.random.rnd
import aBeLibs.svg.Pattern
import aBeLibs.svg.fill
import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.draw.LineCap
import org.openrndr.drawComposition
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.fx.blur.ApproximateGaussianBlur
import org.openrndr.extra.fx.distort.Perturb
import org.openrndr.extra.gui.GUI
import org.openrndr.extra.noise.Random
import org.openrndr.extra.noise.random
import org.openrndr.extra.noise.uniform
import org.openrndr.extra.parameters.ActionParameter
import org.openrndr.extra.parameters.Description
import org.openrndr.extra.parameters.IntParameter
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.namedTimestamp
import org.openrndr.shape.*
import org.openrndr.svg.saveToFile
import java.io.File
import kotlin.math.min
import kotlin.system.exitProcess


/**
 * Creates black and white designs. Applies glitchy effect and blur.
 * Then uses BoofCV to trace contours. Finally, fills contours with patterns.
 *
 * Next: create multiple contours. Subtract to remove overlap.
 */

fun main() = application {
    configure {
        width = 1920
        height = 1080
    }
    program {
        val gui = GUI().apply {
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
        var shape = Shape.EMPTY

        Random.seed = System.currentTimeMillis().toString()

        val params = @Description("Params") object {
            @IntParameter("segmentCount", 5, 30)
            var segmentCount = 10
        }

        fun Drawer.parallels(t: List<Pair<Vector2, Vector2>>) {
            val s = if(Random.bool()) {
                val a = Random.double(0.3, 0.7)
                val b = Random.double(0.3, 0.7)
                listOf(
                    LineSegment(t[0].first, t[0].second).sub(a, b),
                    LineSegment(t[1].first, t[1].second).sub(a, b)
                )
            } else {
                val lim = Random.double(0.1, 0.4)
                listOf(
                    LineSegment(t[0].first, t[1].first).sub(0.0, lim),
                    LineSegment(t[0].second, t[1].second).sub(0.0, lim)
                )
            }
            stroke = ColorRGBa.BLACK
            strokeWeight = 6.0
            val rep = min(
                s[0].start.distanceTo(s[1].start),
                s[0].end.distanceTo(s[1].end)
            ).toInt() / Random.int(10, 40)
            doubleRepeat(rep, 0.2, 0.8) {
                lineSegment(s[0] * (1 - it) + s[1] * it)
            }
        }

        fun newImage2() {
            val colors = listOf(ColorRGBa.BLACK, ColorRGBa.WHITE)
            val angles = listOf(0.0, 60.0, 120.0, 180.0, 240.0, 300.0)
            //val angles = listOf(15.0, 105.0, 195.0, 285.0)
            val lengths = listOf(125.0, 250.0)

            // Populate [segments] (the skeleton of the shape)
            val spine = mutableListOf<Segment>()
            val okArea = drawer.bounds.offsetEdges(-100.0)
            while (spine.size < params.segmentCount) {
                if (spine.isEmpty()) {
                    val start = okArea.position(0.5, 0.5)
                    val end = (start + Polar(
                        angles.random(), lengths.random()
                    ).cartesian).clamp(okArea)
                    spine.add(Segment(start, end))
                } else {
                    // A random point in a spine segment, some segments more likely
                    val start = spine
                        .random()
                        //.randomWithWeights(List(spine.size) { i -> 1.0 + i * i })
                        .position(listOf(0.0, 1.0).random())

                    val end = (start + Polar(
                        angles.random(), lengths.random()
                    ).cartesian).clamp(okArea)

                    val segNew = Segment(start, end)
                    if (spine.all { segOld ->
                            segOld.nearest(end).position.distanceTo(end) > 80.0 &&
                                    segOld.intersections(segNew).none {
                                        it.b.segmentT > 0
                                    }
                        }) {
                        spine.add(segNew)
                    }
                }
            }

            drawer.isolatedWithTarget(bw) {
                clear(colors.first())
                stroke = null

                // Circles
                spine.forEachIndexed { i, it ->
                    fill = colors[i % 2]
                    stroke = colors[(i + 1) % 2]
                    strokeWeight = random(8.0, 24.0)
                    circle(it.start, 120.0 - i * 8)
                    strokeWeight = random(8.0, 24.0)
                    circle(it.end, 110.0 - i * 8)
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

                    val t = c0.tangents(c1) // 0 1 3 2
                    parallels(t)
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
            shape = Shape(bwBlurred.toContours(0.5).map {
                it.smoothed(2)
            })
        }

        fun shapeToSVG() {
            svg.draw {
                fill = null
                stroke = ColorRGBa.BLACK
                shape(shape)
            }
        }

        newImage2()

        val actions = @Description("Actions") object {
            @ActionParameter("A1. new image", 1)
            fun doNew() {
                Random.seed = System.currentTimeMillis().toString()
                newImage2()
            }

            @ActionParameter("A2. to curves", 2)
            fun doCreateContours() {
                svg.clear()
                imageToShape()
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

            @ActionParameter("A3. pattern fill", 3)
            fun doFill() {
                patterns.clear()
                patterns.fill(
                    shape, Pattern.NOISE(
                        2.0, 1.0, 30.0,
                        20.0, 0.008
                    )
                )
            }

            @ActionParameter("B. multilayer shape", 4)
            fun doMultilayer() {
                svg.clear()
                val shapes = List(3) {
                    Random.seed = (frameCount + it).toString()
                    newImage2()
                    imageToShape()
                    shape
                }

                val a = Shape.compound(drawComposition {
                    shape(shapes[0])
                    clipMode = ClipMode.DIFFERENCE
                    shape(shapes[1])
                    shape(shapes[2])
                }.findShapes().map { it.shape })
                val b = Shape.compound(drawComposition {
                    shape(shapes[1])
                    clipMode = ClipMode.DIFFERENCE
                    shape(shapes[2])
                }.findShapes().map { it.shape })
                val c = shapes[2]

                Pattern.stroke = true
                patterns.clear()
                patterns.fill(
                    a, Pattern.NOISE(
                        2.0,
                        1.0,
                        30.0,
                        20.0 rnd 30.0,
                        0.006 rnd 0.009
                    )
                )
                patterns.fill(
                    b, Pattern.NOISE(
                        2.0,
                        0.6,
                        150.0,
                        20.0 rnd 30.0,
                        0.010 rnd 0.015
                    )
                )
                patterns.fill(
                    c, Pattern.HAIR(
                        5.0,
                        0.001,
                        8.0
                    )
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
        keyboard.keyDown.listen {
            when (it.key) {
                KEY_ESCAPE -> exitProcess(0)
            }
        }
        gui.loadParameters(File("data/parameters/Boofcvbw001.json"))
    }
}
