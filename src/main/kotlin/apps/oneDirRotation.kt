package apps

import org.openrndr.KEY_ENTER
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.LineJoin
import org.openrndr.draw.isolated
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.Random
import org.openrndr.extra.noise.uniformRing
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.math.map
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.contour

/**
 * id: e53f85f9-810b-46da-9356-5aef83d1849a
 * description: One direction rotation doodles
 * Looks like paper-clips
 * tags: #new
 */

fun main() = application {
    configure {
        width = 1600
        height = 1000
    }

    program {
        val contour1 = mutableListOf<ShapeContour>()
        val contour2 = mutableListOf<ShapeContour>()

        fun rebuild() {
            contour1.clear()
            contour2.clear()

            for (i in 0 until 9) {
                val points = mutableListOf<Vector2>()
                var base = Vector2.ZERO
                var dir = Polar(Random.double0(360.0))
                var dist = 0.0
                for (j in 0 until 10) {
                    points.add(base + dir.cartesian * dist)
                    if (Random.bool(0.33)) {
                        base += Vector2.uniformRing(90.0, 110.0)
                        dir = Polar(Random.double0(360.0))
                        dist = 0.0
                    } else {
                        dir += Polar(Random.double(170.0, 190.0), 0.0)
                        dist += 10.0
                    }
                }
                //val points = Vector2.uniformsRing(6, 25.0, 100.0)
                val c = ShapeContour.fromPoints(points, true)

                contour1.add(c)
                contour2.add(contour {
                    c.segments.forEachIndexed { i, curr ->
                        val currOffsetDist = 25.0 * Random.simplex(curr.start) + 30.0
                        val currOffset = curr.offset(currOffsetDist)[0]
                        moveOrLineTo(currOffset.start)
                        moveOrLineTo(currOffset.end)

                        val next = c.segments[(i + 1) % c.segments.size]
                        val nextOffsetDist = 25.0 * Random.simplex(next.start) + 30.0
                        val nextOffset = next.offset(nextOffsetDist)[0]
                        val distance = currOffset.end.distanceTo(nextOffset.start)
                        curveTo(
                            currOffset.end + currOffset.direction(1.0) * (distance * 0.6),
                            nextOffset.start - nextOffset.direction(0.0) * (distance * 0.6),
                            nextOffset.start
                        )
                    }
                    close()
                })
            }
        }
        rebuild()

        extend(Screenshots())
        extend {
            drawer.run {
                clear(ColorRGBa.PINK)
                strokeWeight = 2.0
                lineJoin = LineJoin.BEVEL

                contour1.forEachIndexed { i, c ->
                    isolated {
                        translate(
                            width * map(0.0, 2.0, 0.2, 0.8, i % 3.0) - c.bounds.center.x,
                            height * map(0.0, 2.0, 0.2, 0.8, (i / 3) * 1.0) - c.bounds.center.y
                        )
                        fill = null
                        stroke = ColorRGBa.WHITE
                        contour(c)

                        fill = ColorRGBa.PINK
                        circles(c.segments.map { it.start }, 6.0)

                        fill = null
                        stroke = ColorRGBa.BLACK
                        contour(contour2[i])

                        fill = ColorRGBa.PINK
                        circles(contour2[i].segments.map { it.start }, 6.0)
                    }
                }
            }
        }

        keyboard.keyDown.listen {
            when (it.key) {
                KEY_ENTER -> rebuild()
            }
        }
        window.moved.listen {
            println("Doesn't work on my laptop (i3?)")
        }
    }
}
