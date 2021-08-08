package apps2

import aBeLibs.geometry.intersections
import aBeLibs.math.angle
import org.openrndr.MouseButton
import org.openrndr.applicationSynchronous
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.LineJoin
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.Random
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.math.mod_
import org.openrndr.shape.Circle
import org.openrndr.shape.LineSegment
import org.openrndr.shape.ShapeContour
import kotlin.math.PI
import kotlin.math.abs

/**
 * Interactive, animated growth algorithm.
 * Inserts points on a closed shape trying to maintain a minimum
 * distance and a maximum angle (sharpness) between the points
 */
fun main() = applicationSynchronous {
    configure {
        width = 512
        height = 512
    }

    program {
        val num = 5
        val points = MutableList(num) {
            Polar(it * 360.0 / num, Random.double(60.0, 80.0)).cartesian
        }
        val targets = mutableListOf<Vector2>()
        val desiredSep = 10.0
        var shp = ShapeContour.fromPoints(points, true)

        fun separate() {
            points.forEachIndexed { j, p ->
                points.forEachIndexed { i, other ->
                    val nexti = (i + 1) % points.size
                    if (j != i && j != nexti) {
                        val otherNext = points[nexti]
//                        val sep = LineSegment(other, otherNext).distance(p)
                        val nearest = LineSegment(other, otherNext).nearest(p)
                        val sep = p.distanceTo(nearest)
                        if (sep < desiredSep) {
//                            val k = 0.1 * desiredSep / sep
//                            points[j] += (p - other).normalized * k + (p - otherNext).normalized * k
                            points[j] += (p - nearest).normalized * 0.15 * desiredSep / sep
                        }

                    }
                }
            }
        }

        fun maintainAngles() {
            points.forEachIndexed { i, p ->
                val next = points.nextWrapped(i)
                val prev = points.prevWrapped(i)
                val a = abs(abs(angle(p, prev, next)) - PI)
                if (a > Math.toRadians(30.0)) {
                    points[i] = p.mix((prev + next) * 0.5, a / 4)
                }
            }
        }


        fun maintainDistance() {
            points.forEachIndexed { i, p ->
                val next = points.nextWrapped(i)
                val prev = points.prevWrapped(i)
                if (prev.squaredDistanceTo(next) < desiredSep * desiredSep) {
                    Circle(prev, desiredSep * 2).intersections(
                        Circle(next, desiredSep * 2)
                    ).minByOrNull { it.squaredDistanceTo(p) }?.run {
                        points[i] = p.mix(this, 0.1)
                    }
                }
            }
        }

        fun applyWind() {
            points.forEachIndexed { i, p ->
                val d = abs(p.x - shp.bounds.center.x)
                if (d < 50) {
                    val f = 1 - d / 50
                    points[i] -= Vector2(0.0, f * 0.2)
                }
            }
        }

        fun applyNoise() {
            points.forEachIndexed { i, p ->
                val pc = p * 0.01
                points[i] += Vector2(
                    Random.simplex(pc.x, pc.y, seconds),
                    Random.simplex(pc.y, -pc.x)
                ) * 0.1
            }
        }

        fun addPoint() {
            val i = (points.size + points.size * Random.simplex(0.0, frameCount * 0.001)).toInt().mod_(points.size)
            val iNext = (i + 1) % points.size
            //val d = points[i].distanceTo(points[iNext]) * 0.001
            val newPoint = (points[i] + points[iNext]) * 0.5 // + Random.vector2() * d
            points.add(iNext, newPoint)
        }

        extend(Screenshots())
        //extend(ScreenRecorder())
        extend {
            if (mouse.pressedButtons.contains(MouseButton.LEFT)) {
                addPoint()
            }
            targets.clear()
            applyNoise()
            applyWind()
            maintainAngles()
            maintainDistance()
            separate()
            shp = ShapeContour.fromPoints(points, true)

            drawer.run {
                clear(ColorRGBa.fromHex("4D0C47"))
                translate(bounds.center - shp.bounds.center)
                stroke = null
                fill = ColorRGBa.fromHex("C2E881")
                lineJoin = LineJoin.BEVEL
                contour(shp)
            }
        }
    }
}

private fun List<Vector2>.nextWrapped(i: Int) = this[(i + 1) % size]
private fun List<Vector2>.prevWrapped(i: Int) = this[(i - 1 + size) % size]
