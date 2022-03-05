package latest

import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolated
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.shapes.bezierPatch
import org.openrndr.math.Vector2
import org.openrndr.shape.Segment
import org.openrndr.shape.ShapeContour

/**
 * id: bb05a8b3-5c8f-4872-97f6-cfb176883801
 * description: New sketch
 * tags: #new
 */

fun main() = application {
    program {
        // helper to get screen locations using normalized uv values
        fun pos(u: Double, v: Double) = drawer.bounds.position(u, v)
        val c0 = Segment(
            pos(0.1, 0.1),
            pos(0.5, 0.3),
            pos(0.9, 0.1)
        )
        val c1 = Segment(
            pos(0.0, 0.4),
            pos(0.3, 0.5),
            pos(0.7, 0.4),
            pos(0.9, 0.5)
        )
        val c2 = Segment(
            pos(0.1, 0.7),
            pos(0.5, 0.7),
            pos(0.6, 0.6)
        )
        val c3 = Segment(
            pos(0.1, 0.9),
            pos(0.2, 0.8),
            pos(0.8, 1.0),
            pos(0.9, 0.9)
        )

        val bp = bezierPatch(c0, c1, c2, c3)

        val sourceContour = c3.reverse.contour +
                Segment(c3.start, c2.start, c1.start, c0.start).contour +
                c0.contour

        val closingContour = Segment(c3.end, c2.end, c1.end, c0.end).contour

        extend(Screenshots())
        extend {
            drawer.isolated {
                clear(ColorRGBa.PINK)

                stroke = ColorRGBa.RED
                strokeWeight = 5.0
                contour(sourceContour)

                stroke = ColorRGBa.BLUE
                contour(closingContour)

                stroke = ColorRGBa.YELLOW
                strokeWeight = 3.0
                segments(listOf(c0, c1, c2, c3))

                stroke = ColorRGBa.BLACK.opacify(0.2)
                strokeWeight = 1.0
                for (i in 1..29) {
                    contour(bp.horizontal(i / 30.0))
                    contour(bp.vertical(i / 30.0))
                }

                val curves = List(50) {
                    val u = it / 50.0
                    val p0 = sourceContour.position(u)
                    val p1 = closingContour.position(u)
                    val p0n = sourceContour.interpolatedNormal(u)
                    val p1n = closingContour.normal(u)
                    val k = p0.distanceTo(p1) * 0.3
                    Segment(p0, p0 - p0n * k, p1 + p1n * k, p1).contour
                }
                stroke = ColorRGBa.BLACK
                contours(curves)
            }
        }

        keyboard.keyDown.listen {
            when (it.key) {
                KEY_ESCAPE -> {
                    application.exit()
                }
            }
        }
    }
}

private fun ShapeContour.interpolatedNormal(u: Double): Vector2 {
    val (segI, segOff) = segment(u)
    val sz = segments.size
    val currN = normal(u)
    return if (segOff < 0.5) {
        val prevCornerAvg = segments[segI].normal(0.0).mix(
            segments[(segI - 1).coerceAtLeast(0)].normal(1.0), 0.5
        )
        prevCornerAvg.mix(currN, segOff * 2)
    } else {
        val nextCornerAvg = segments[segI].normal(1.0).mix(
            segments[(segI + 1).coerceAtMost(sz - 1)].normal(0.0), 0.5
        )
        currN.mix(nextCornerAvg, segOff * 2 - 1)
    }
}
