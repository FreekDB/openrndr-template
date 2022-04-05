package apps.p5

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.Random
import org.openrndr.math.Vector2
import org.openrndr.shape.ShapeContour

/**
 * id: 089c59e5-6c8a-44e6-b4bd-0d312745d198
 * description: lineNormals, ported from
 * https://github.com/hamoid/Fun-Programming/tree/master/processing/ideas/2018/11/lineNormals
 * tags: #new
 */

fun main() = application {
    configure {
        width = 400
        height = 400
    }

    program {
        val points = listOf(
            Vector2(61.0, 183.0),
            Vector2(108.0, 113.0),
            Vector2(193.0, 118.0),
            Vector2(256.0, 158.0),
            Vector2(248.0, 239.0),
            Vector2(258.0, 310.0),
            Vector2(328.0, 353.0),
            Vector2(377.0, 341.0)
        )

        // -------------------
        // A. simpler approach with same offset for all vertices. Creates some extra points.
//        val thick = ShapeContour.fromPoints(
//            points + points.subList(1, points.size - 1).reversed(), true
//        ).offset(15.0, SegmentJoin.MITER)

        // -------------------
        // B. more complex approach imitating the original behavior
        // in which each vertex has a different offset.
        // Create a contour we can query.
        val polyline = ShapeContour.fromPoints(points, false)

        var normals = listOf(polyline.segments.first().normal(0.0)) +
                polyline.segments.zipWithNext { a, b -> (a.normal(1.0) + b.normal(0.0)).normalized } +
                polyline.segments.last().normal(1.0)

        normals = normals.map { it * Random.double(10.0, 30.0) }

        // construct shape. First add one side, then the other in reverse.
        val thick = ShapeContour.fromPoints(
            points.mapIndexed { i, p ->
                p + normals[i]
            } + points.reversed().mapIndexed { i, p ->
                p - normals.reversed()[i]
            }, true
        )

        // -------------------
        extend(Screenshots())
        extend {
            drawer.run {
                clear(ColorRGBa.WHITE)

                // draw calculated thick line
                stroke = null
                fill = ColorRGBa.fromHex("FFCC00")
                contour(thick)

                // draw the spine
                stroke = ColorRGBa.fromHex("552200")
                fill = null
                lineStrip(points)

                // draw the spine vertices
                fill = ColorRGBa.WHITE
                stroke = ColorRGBa.fromHex("552200")
                strokeWeight = 2.0
                points.forEach { circle(it, 5.0) }

                // draw the contour points
                stroke = null
                fill = ColorRGBa.fromHex("883300")
                thick.segments.forEach {
                    circle(it.start, 4.0)
                }
            }
        }
    }
}
