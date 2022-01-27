package latest

import aBeLibs.geometry.dedupe
import org.openrndr.KEY_ENTER
import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.noise.Random
import org.openrndr.extra.noise.gradientPerturbFractal
import org.openrndr.extra.noise.poissonDiskSampling
import org.openrndr.extra.triangulation.Delaunay
import org.openrndr.namedTimestamp
import org.openrndr.shape.Circle
import org.openrndr.shape.draw
import org.openrndr.shape.drawComposition
import org.openrndr.svg.saveToFile
import java.io.File

fun main() =
    application {
        configure {
            width = 900
            height = 900
        }
        program {
            val svg = drawComposition { }

            fun newDesign() {
                val circle = Circle(drawer.bounds.center, width * 0.32)
                val points = poissonDiskSampling(
                    drawer.bounds, 15.0
                ) { v ->
                    val perturb = gradientPerturbFractal(
                        seconds.toInt(),
                        position = v * 0.008
                    )
                    Random.simplex(perturb) < Random.double(-1.0, 0.0)
                }
                val delaunay = Delaunay.from(
                    points + circle.contour.equidistantPositions(40)
                )
                val voronoi = delaunay.voronoi(drawer.bounds.scale(0.8))
                val contours = voronoi.cellsPolygons()
                val centers = contours.map { contour ->
                    contour.segments.map { it.start }.reduce { sum, element ->
                        sum + element
                    } / contour.segments.size.toDouble()
                }
                val radii = contours.mapIndexed { i, c ->
                    c.nearest(centers[i]).position.distanceTo(centers[i])
                }

                svg.clear()
                svg.draw {
                    fill = null
                    stroke = ColorRGBa.PINK
                    //contours(contours)
                    circles(centers, radii)
                }
            }

            extend {
                drawer.clear(ColorRGBa.BLACK)
                drawer.composition(svg)
            }

            mouse.buttonDown.listen {
                newDesign()
            }

            keyboard.keyDown.listen {
                when (it.key) {
                    KEY_ENTER -> {
                        val fileName = program.namedTimestamp("svg", "print")
                        svg.dedupe().saveToFile(File(fileName))
                        println("Saved as $fileName")
                    }
                    KEY_ESCAPE -> application.exit()
                }
            }
        }
    }

