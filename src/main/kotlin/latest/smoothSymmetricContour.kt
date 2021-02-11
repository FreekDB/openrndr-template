package latest

import aBeLibs.geometry.randomPoint
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolated
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.Random
import org.openrndr.math.Vector2
import org.openrndr.math.map
import org.openrndr.shape.*
import java.lang.Double.min

/**
 * In this program I'm going to work with a contour.
 * I want to do something similar to .fromPoints() but
 * I don't want a polygonal look, but smoothed instead.
 * I can thing of 4 approaches:
 * - completely automatic
 * - set the width for each vertex
 * - set the width on each side of each vertex
 * - set widths and tilt
 */

fun main() = application {
    configure {
        width = 1500
        height = 400
    }
    program {
        fun ShapeContour.symmetrize(roundness: Double = 0.1): Pair<ShapeContour, List<LineSegment>> {
            val visibleTangents = mutableListOf<LineSegment>()
            val tangents = segments.mapIndexed { i, curr ->
                val next = segments[(i - 1 + segments.size) % segments.size]
                (curr.direction()).mix(next.direction(), 0.5).normalized
            }
            val newSegments = segments.mapIndexed { i, currSegment ->
                val l = currSegment.length
                val j = (i + 1 + segments.size) % segments.size
                val c0 = currSegment.start + tangents[i] * l * roundness
                val c1 = currSegment.end - tangents[j] * l * roundness
                visibleTangents.add(LineSegment(currSegment.start, c0))
                visibleTangents.add(LineSegment(c1, currSegment.end))
                visibleTangents.add(
                    LineSegment(
                        currSegment.start,
                        currSegment.end
                    )
                )
                Segment(currSegment.start, c0, c1, currSegment.end)
            }
            return Pair(ShapeContour(newSegments, closed), visibleTangents)
        }

        fun softies(): List<Pair<ShapeContour, List<LineSegment>>> {
            do {
                val numPoints = Random.int(3, 8)
                val points = List(numPoints) {
                    Rectangle.fromCenter(Vector2.ZERO, 300.0).randomPoint()
                }
                val center = points.bounds.center
                val scale = min(
                    250 / points.bounds.width,
                    250 / points.bounds.height
                )
                val pointsCentered = points.map { (it - center) * scale }
                val c = ShapeContour.fromPoints(pointsCentered, true)
                // NOTE: .intersections() can return 0 intersections even if
                // there is one: https://github.com/lacuna/artifex/issues/3
                if (c.intersections(c).isEmpty()) {
                    return List(5) {
                        c.symmetrize(0.05 + it * 0.1)
                    }
                }
            } while (true)
        }

        var softies = softies()
        extend(Screenshots())
        extend {
            drawer.apply {
                clear(ColorRGBa.WHITE)
                fill = ColorRGBa.PINK
                softies.forEachIndexed { i, it ->
                    isolated {
                        translate(
                            i.toDouble().map(
                                0.0, softies.size - 1.0,
                                150.0, width - 150.0
                            ), height * 0.5
                        )
                        stroke = null
                        contour(it.first)
                        stroke = ColorRGBa.BLACK.opacify(0.4)
                        lineSegments(it.second)
                    }
                }
            }
        }
        mouse.buttonDown.listen {
            softies = softies()
        }
    }
}
