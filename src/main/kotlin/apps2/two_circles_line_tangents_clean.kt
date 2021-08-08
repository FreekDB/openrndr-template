package apps2

import aBeLibs.geometry.round
import aBeLibs.geometry.tangentWrapConcave
import org.openrndr.applicationSynchronous
import org.openrndr.color.ColorHSLa
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.renderTarget
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.Random
import org.openrndr.extra.noise.uniform
import org.openrndr.math.Vector2
import org.openrndr.math.map
import org.openrndr.panel.elements.round
import org.openrndr.shape.Circle
import org.openrndr.shape.Rectangle
import org.openrndr.shape.map

/**
 * Ported from
 * https://github.com/hamoid/Fun-Programming/tree/master/processing/ideas/2016/07/two_circles_line_tangents
 * then added concave and convex circle tangents.
 */

fun main() = applicationSynchronous {
    configure {
        width = 1920
        height = 1080
    }
    program {
        val rt = renderTarget(width, height) {
            colorBuffer()
            depthBuffer()
        }

        extend(Screenshots())
        extend {
            drawer.image(rt.colorBuffer(0))
        }

        keyboard.keyDown.listen {
            when (it.name) {
                "r" -> {
                    drawer.isolatedWithTarget(rt) {
                        clear(ColorRGBa.WHITE)
                        stroke = null

                        val baseHue = Random.double0(360.0)
                        val colors = List(5) {
                            val h = (baseHue + Random.int0(2) * 180.0) % 360.0
                            val s = Random.int0(3) * 0.3 + 0.1
                            val l = Random.double(0.2, 0.9).round(1)
                            ColorHSLa(h, s, l).toRGBa()
                        }

                        val from = Rectangle.fromCenter(Vector2.ZERO, 2.0, 2.0)

                        val columns = 3
                        val rows = 1
                        for (i in 0 until 3) {
                            val yy = map(
                                0.0,
                                rows - 1.0,
                                height * 0.5,
                                height * 0.5,
                                (i / columns).toDouble()
                            )
                            val xx = map(
                                0.0,
                                columns - 1.0,
                                width * 0.25,
                                width * 0.75,
                                (i % columns).toDouble()
                            )
                            val to = Rectangle.fromCenter(
                                Vector2(xx, yy),
                                height * 0.3,
                                height * 0.3
                            )

                            var found = 0
                            while (found < 15) {
                                fill = Random.pick(colors)

                                val p0 =
                                    Vector2.uniform(-Vector2.ONE, Vector2.ONE)
                                        .round(0)
                                val p1 =
                                    Vector2.uniform(-Vector2.ONE, Vector2.ONE)
                                        .round(0)
                                val d = p0.distanceTo(p1)
                                if (d > 0.9 && d < 1.5) {
                                    val leftCirc = Circle(
                                        p0.map(from, to),
                                        to.height * 0.2 - found * to.height * 0.02
                                    )
                                    val rightCirc = Circle(
                                        p1.map(from, to),
                                        to.height * 0.1 - found * to.height * 0.01
                                    )
                                    if(leftCirc.radius < 1 || rightCirc.radius < 1) {
                                        break
                                    }
                                    contour(
                                        tangentWrapConcave(
                                            rightCirc,
                                            leftCirc,
                                            Random.double(
                                                to.height * 0.5,
                                                to.height * 0.7
                                            )
                                        )
                                    )
                                    found++
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
