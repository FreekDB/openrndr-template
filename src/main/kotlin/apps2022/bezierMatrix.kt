package apps2022

import org.openrndr.KEY_ENTER
import org.openrndr.WindowMultisample
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.LineJoin
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.poissonDiskSampling
import org.openrndr.extra.shapes.grid
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import org.openrndr.shape.Segment
import kotlin.math.min

/**
 * id: 7045d6c1-6f23-4b23-9ab6-5a1d1faa243c
 * description: New sketch
 * tags: #new
 */

fun main() = application {
    configure {
        width = 1500
        height = 1200
        multisample = WindowMultisample.SampleCount(8)
    }

    program {
        val segments = mutableListOf<Segment>()

        fun segmentFromPoints(p: List<Vector2>) =
            Segment(p[0], p[1], p[2], p[3])

        fun gen(bounds: Rectangle, sep: Double): List<Segment> {
            val points = poissonDiskSampling(
                bounds, sep, 20
            ).shuffled()
            val waypoints = List(4) {
                segmentFromPoints(points.subList(it * 4, it * 4 + 4))
            }
            val num = min(waypoints[0].length, waypoints[3].length).toInt() / 3
            return List(num) { i ->
                val t = i / (num - 1.0)
                segmentFromPoints(waypoints.map { it.position(t) })
            }
        }

        extend(Screenshots())
        extend {
            drawer.clear(ColorRGBa.WHITE)
            drawer.stroke = ColorRGBa.BLACK.opacify(0.5).alphaMultiplied
            drawer.lineJoin = LineJoin.ROUND
            segments.forEach { drawer.segment(it) }
        }

        keyboard.keyDown.listen {
            if (it.key == KEY_ENTER) {
                segments.clear()
                drawer.bounds.grid(
                    4, 3,
                    30.0, 30.0
                ).flatten().forEach { rect ->
                    segments.addAll(gen(rect, 5.0))
                    segments.addAll(gen(rect, 5.0))
                }
            }
        }
    }
}
