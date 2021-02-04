package latest

import aBeLibs.geometry.randomPoint
import aBeLibs.geometry.selfIntersections
import org.openrndr.application
import org.openrndr.configuration
import org.openrndr.extra.noise.Random
import org.openrndr.shape.Segment
import org.openrndr.shape.ShapeContour

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
    configuration {
        width = 800
        height = 800
    }
    program {
        fun softie() :ShapeContour {
            do {
                val points = List(5) { drawer.bounds.randomPoint() }
                val c = ShapeContour.fromPoints(points, true)
                // FIXME: this is not working well, there are
                // intersection sometimes in the resulting shape.
                // Maybe when it's between the last and the first point?
                // In other words, does it account for closed shapes?
                if(c.selfIntersections().isEmpty()) {
                    return c.symmetrize()
                }
            } while(true)
        }

        var contour = softie()
        extend {
            drawer.contour(contour)
        }
        mouse.buttonDown.listen {
            contour = softie()
        }
    }
}

private fun ShapeContour.symmetrize(): ShapeContour {
    val newSegments = segments.map {
        val mid = (it.start + it.end) / 2.0
        val dist = it.start.distanceTo(it.end)
        Segment(
            it.start,
            mid + Random.vector2() * dist * 0.1,
            mid + Random.vector2() * dist * 0.1,
            it.end
        )
    }
    return ShapeContour(newSegments, true)
}
