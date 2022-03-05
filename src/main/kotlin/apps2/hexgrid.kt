package apps2

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.Random
import org.openrndr.math.Vector2

/**
 * id: 423646ef-0198-4372-8580-902376e1a0be
 * description: Simple non-configurable hex grid
 * tags: #new
 */

fun main() = application {
    program {

        val sep = drawer.height * 0.2
        val waypoints = mutableListOf<Vector2>()

        for (x in -3 until 3) {
            for (y in -2 until 3) {
                val off = if (y % 2 == 0) 0.8 else 0.3
                val p = Vector2(x * 1.0 + off, y * 0.85)
                if (Vector2.ZERO.distanceTo(p) < 2.3) {
                    waypoints.add(p * sep + drawer.bounds.center)
                }
            }
        }

        val numPoints = waypoints.size
        val waypointsNext = waypoints.map { current ->
            waypoints.indices.filter { other ->
                val d = current.distanceTo(waypoints[other])
                d > 0.1 && d < sep * 1.1
            }.shuffled().take(2)
        }

        extend(Screenshots())
        extend {
            drawer.run {
                clear(ColorRGBa.WHITE)
                circles(waypoints, 20.0)
                waypointsNext.forEachIndexed { i, it ->
                    val src = waypoints[i]
                    it.forEach { other ->
                        lineSegment(
                            src + Random.vector2() * 5.0,
                            waypoints[other] + Random.vector2() * 5.0
                        )
                    }
                }
            }
        }
    }
}
