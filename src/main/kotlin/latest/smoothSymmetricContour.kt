package latest

import aBeLibs.geometry.randomPoint
import aBeLibs.geometry.symmetrize
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.color.rgb
import org.openrndr.draw.isolated
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.Random
import org.openrndr.math.IntVector2
import org.openrndr.math.Vector2
import org.openrndr.shape.LineSegment
import org.openrndr.shape.Rectangle
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.bounds
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
    val side = 5
    configure {
        width = 1000
        height = 1000
    }
    program {
        fun createSofties(): List<Pair<ShapeContour, List<LineSegment>>> {
            do {
                val numPoints = Random.int(6, 8)
                val points = List(numPoints) {
                    Rectangle.fromCenter(Vector2.ZERO, 300.0).randomPoint()
                }
                val center = points.bounds.center
                val maxSize = 480.0 / side
                val scale = min(
                    maxSize / points.bounds.width,
                    maxSize / points.bounds.height
                )
                val pointsCentered = points.map { (it - center) * scale }
                val c = ShapeContour.fromPoints(pointsCentered, true)
                // NOTE: .intersections() can return 0 intersections even if
                // there is one: https://github.com/lacuna/artifex/issues/3
                // NOTE2: it's checking if the source shape has intersections
                // but not if the symmetrized versions do.
                if (c.intersections(c).isEmpty()) {
                    val result =
                        mutableListOf<Pair<ShapeContour, List<LineSegment>>>()
                    var tries = 0
                    while (result.size < side * side) {
                        val cSym = c.symmetrize { i ->
                            val n = Random.simplex(
                                result.size * 0.3,
                                i * 0.3 + tries * 7.17
                            ) * 0.4 + 0.5
                            Pair(n, n)
                        }
                        if (cSym.first.intersections(cSym.first).isEmpty() ||
                            tries++ > 5
                        ) {
                            result.add(cSym)
                        }
                    }
                    return result
                }
            } while (true)
        }

        var softies = createSofties()
        extend(Screenshots())
        extend {
            drawer.clear(rgb("322E2A"))
            drawer.fill = rgb("DCA050")
            softies.forEachIndexed { i, it ->
                drawer.isolated {
                    val pos = (IntVector2(
                        i % side,
                        i / side
                    ).vector2 + 1.0) / (side + 1.0)
                    translate(bounds.position(pos.x, pos.y))
                    rotate(180 * Random.simplex(pos * 0.2))
                    stroke = null
                    contour(it.first)
                    if (keyboard.pressedKeys.contains("left-shift")) {
                        stroke = ColorRGBa.WHITE.opacify(0.4)
                        lineSegments(it.second)
                    }
                }
            }
        }
        mouse.buttonDown.listen {
            softies = createSofties()
        }
    }
}
