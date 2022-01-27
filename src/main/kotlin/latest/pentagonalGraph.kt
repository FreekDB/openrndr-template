package latest

import aBeLibs.geometry.chopped
import aBeLibs.geometry.intersections
import aBeLibs.geometry.round
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.DefaultUndirectedGraph
import org.openrndr.KEY_ENTER
import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.color.rgb
import org.openrndr.dialogs.saveFileDialog
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.Random
import org.openrndr.math.IntVector2
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.math.map
import org.openrndr.shape.*
import org.openrndr.svg.saveToFile

/**
 * A simple *test* seems to **work**
 * ![thumbnail](../a.png)
 */
fun main() =
    application {
        configure {
            width = 900
            height = 900
        }
        program {
            Random.seed = System.nanoTime().toString()
            var linesAfter = listOf<Segment>()
            var closestSegment = Segment(Vector2.ZERO, Vector2.ZERO)

            fun newDesign(): Composition {
                val frame = Circle(drawer.bounds.center, 400.0)

                val linesBefore = List(50) {
                    val angle = Random.int0(5) * 72.0
                    val p = drawer.bounds.center +
                            Polar(angle + 90.0, it.toDouble().map(0.0, 39.0,
                                40.0, 300.0)).cartesian
                    val off = Polar(angle, 1000.0).cartesian
                    val cutter = Segment(p + off,p - off)
                    val points = cutter.intersections(frame).map { intersection -> intersection.position }
                    if(Random.bool()) {
                        Segment(points[0], points[0].mix(points[1], Random.double(0.3, 1.0)))
                    } else {
                        Segment(points[1], points[1].mix(points[0], Random.double(0.3, 1.0)))
                    }
                }
                linesAfter = linesBefore.chopped()

                // https://jgrapht.org/
                val g = DefaultUndirectedGraph<IntVector2, DefaultEdge>(DefaultEdge::class.java)
                linesAfter.forEach {
                    val a = it.start.round(1).toInt()
                    val b = it.end.round(1).toInt()
                    g.addVertex(a)
                    g.addVertex(b)
                    g.addEdge(a, b)
                }
                val cycles = abeCycle(g)

                return drawComposition {
                    lineSegments(linesAfter.map { LineSegment(it.start, it.end) })

                    cycles.forEach { cycle ->
                        stroke = ColorRGBa.BLACK
                            //ColorRGBa.fromVector(Random.vector3(0.0, 1.0), 0.6)
                        strokeWeight = 5.0 // Random.double(3.0, 5.0)
                        lineLoop(cycle.map { it.vector2 })
                    }
                }
            }

            var svg = newDesign()

            extend(Screenshots())
            extend {
                drawer.clear(ColorRGBa.PINK)
                drawer.composition(svg)
                drawer.text("$seconds", 20.0, 20.0)
                drawer.text("${closestSegment.start.round(1)}", 20.0, 40.0)
                drawer.text("${closestSegment.end.round(1)}", 20.0, 60.0)
                drawer.stroke = rgb(seconds % 1.0)
                drawer.lineSegment(closestSegment.start, closestSegment.end)
            }

            mouse.moved.listen {
                closestSegment = linesAfter.minByOrNull {
                    LineSegment(it.start, it.end).distance(mouse.position)
                }!!
            }
            mouse.buttonDown.listen {
                svg = newDesign()
            }
            keyboard.keyDown.listen {
                when (it.key) {
                    KEY_ENTER -> saveFileDialog(supportedExtensions = listOf("svg")) { file ->
                        svg.saveToFile(file)
                    }
                    KEY_ESCAPE -> application.exit()
                }
            }
        }
    }

