package latest

import aBeLibs.geometry.randomPoint
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolated
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.Random
import org.openrndr.math.IntVector2
import org.openrndr.math.Vector2
import org.openrndr.shape.*
import java.lang.Double.min

/**
 * In this program I'm going to work with a contour.
 * I want to do something similar to .fromPoints() but
 * I don't want a polygonal look, but smoothed instead.
 * The current version generates the tangent lines, which is
 * not necessary and makes it more complicated, but I wanted
 * to show those lines when holding the left shift key down.
 */

fun main() = application {
    configure {
        width = 1000
        height = 1000
    }
    program {
        fun ShapeContour.symmetrize(roundness: (Int) -> Pair<Double, Double>):
                Pair<ShapeContour, List<LineSegment>> {
            val visibleTangents = mutableListOf<LineSegment>()
            val tangents = segments.mapIndexed { i, curr ->
                val next = segments[(i - 1 + segments.size) % segments.size]
                (curr.direction()).mix(next.direction(), 0.5).normalized
            }
            val newSegments = segments.mapIndexed { i, currSegment ->
                val sz = segments.size
                val len = currSegment.length
                val iNext = (i + 1 + sz) % sz
                val c0 = currSegment.start +
                        tangents[i] * len * roundness(i).first
                val c1 = currSegment.end -
                        tangents[iNext] * len * roundness((i + 1) % sz).second
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
                val numPoints = Random.int(6, 8)
                val points = List(numPoints) {
                    Rectangle.fromCenter(Vector2.ZERO, 300.0).randomPoint()
                }
                val center = points.bounds.center
                val maxSize = 120.0
                val scale = min(
                    maxSize / points.bounds.width,
                    maxSize / points.bounds.height
                )
                val pointsCentered = points.map { (it - center) * scale }
                val c = ShapeContour.fromPoints(pointsCentered, true)
                // NOTE: .intersections() can return 0 intersections even if
                // there is one: https://github.com/lacuna/artifex/issues/3
                if (c.intersections(c).isEmpty()) {
                    return List(16) {
                        c.symmetrize { i ->
                            val n =
                                Random.simplex(it * 0.3, i * 0.3) * 0.4 + 0.5
                            Pair(n, n)
                        }
                    }
                }
            } while (true)
        }

        var softies = softies()
        extend(Screenshots())
        extend {
            drawer.clear(ColorRGBa.WHITE)
            drawer.fill = ColorRGBa.PINK
            softies.forEachIndexed { i, it ->
                drawer.isolated {
                    val pos = (IntVector2(i % 4, i / 4).vector2 + 1.0) / 5.0
                    translate(bounds.position(pos.x, pos.y))
                    stroke = null
                    contour(it.first)
                    if (keyboard.pressedKeys.contains("left-shift")) {
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
