package latest

import aBeLibs.lang.doubleRepeat
import aBeLibs.geometry.bentFromPoints
import org.openrndr.KEY_ENTER
import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extensions.Screenshots
import org.openrndr.math.Vector2
import org.openrndr.shape.Segment
import org.openrndr.shape.ShapeContour

fun main() = application {
    configure {
        width = 800
        height = 800
    }

    program {
        val points = mutableListOf<Vector2>()
        var sourceContour = ShapeContour.EMPTY
        val curves = mutableListOf<ShapeContour>()

        fun build() {
            if (points.size in 2..4) {
                sourceContour += ShapeContour.bentFromPoints(
                    points[points.size - 2],
                    points.last(),
                    0.1
                )
            }
            if (sourceContour.segments.size == 3) {
                val s0 = sourceContour.segments.first()
                val s2 = sourceContour.segments.last()
                val curvedSourceContour =
                    s0.contour + Segment(
                        s0.end,
                        s0.end + (s0.end - s0.control.last()) * 3.0,
                        s2.start + (s2.start - s2.control.first()) * 3.0,
                        s2.start
                    ).contour + s2.contour

                val closingContour = ShapeContour.bentFromPoints(
                    sourceContour.position(0.0),
                    sourceContour.position(1.0),
                    0.3
                )
                doubleRepeat(100) { u ->
                    val p0 = sourceContour.position(u)
                    val p1 = closingContour.position(u)
                    val p0n = curvedSourceContour.normal(u)
                    val p1n = closingContour.normal(u)
                    val k = p0.distanceTo(p1) * 0.3
                    val s = Segment(p0, p0 - p0n * k, p1 + p1n * k, p1).contour
                    curves.add(s)
                }
            }
        }

        extend(Screenshots())
        extend {
            drawer.stroke = ColorRGBa.WHITE
            drawer.circles(points, 10.0)
            drawer.contour(sourceContour)
            drawer.contours(curves)
        }

        mouse.buttonDown.listen {
            points.add(it.position)
            build() // TODO: remove
        }

        keyboard.keyDown.listen {
            when (it.key) {
                KEY_ESCAPE -> {
                    application.exit()
                }
                KEY_ENTER -> {
                    build()
                    points.clear()
                }
            }
        }
    }
}
