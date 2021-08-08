package apps2.simpleTests

import aBeLibs.geometry.randomPoint
import org.openrndr.applicationSynchronous
import org.openrndr.color.ColorRGBa
import org.openrndr.extensions.Screenshots
import org.openrndr.shape.LineSegment

fun main() = applicationSynchronous {
    program {

        val segments = mutableListOf<LineSegment>()

        segments.add(
            LineSegment(
                drawer.bounds.randomPoint(),
                drawer.bounds.randomPoint()
            )
        )
        for(i in 0 until 10) {
            val pc = i / 9.0
            val p = segments[0].position(pc)
            val normal = segments[0].normal // not .normal(pc)!!!
            segments.add(
                LineSegment(
                    p + (normal * 3.0), p + (normal * 33.0)
                )
            )
        }

        extend(Screenshots())
        extend {
            drawer.run {
                clear(ColorRGBa.WHITE)
                lineSegments(segments)
            }
        }
    }
}
