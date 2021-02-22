package latest

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolated
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.Random
import org.openrndr.extra.shadestyles.LinearGradient
import org.openrndr.extra.shapes.regularPolygon
import org.openrndr.extras.color.presets.CYAN
import org.openrndr.extras.color.presets.MAGENTA
import org.openrndr.math.Vector2
import kotlin.math.sin
import kotlin.math.sqrt

// Copycat game, Creative Code Jam 20.02.2021
// Coded based on an oral description of
// https://beesandbombs.tumblr.com/post/57971648339/hexagons-pulsin
// by Claudine

fun main() = application {
    program {
        val s = 50.0
        val h = 0.5 * s * sqrt(3.0)
        val hexagon = regularPolygon(6, Vector2.ZERO, s)
        val gradient = LinearGradient(ColorRGBa.WHITE, ColorRGBa.WHITE)

        extend(Screenshots())
        extend {
            drawer.apply {
                clear(ColorRGBa.CYAN)
                stroke = ColorRGBa.WHITE.opacify(0.04)

                for (x in 0 until width step (s * 3).toInt()) {
                    for (y in 0 until (height / h).toInt() + 1) {
                        val even = y % 2 == 0
                        val xd = x.toDouble() + if (even) s * 1.5 else 0.0
                        val yd = y * h
                        isolated {
                            translate(xd, yd)
                            val k = 0.5 + 0.5 * sin(Random.simplex(xd, yd) * 3 + seconds * 3)
                            scale(k)
                            gradient.color1 = ColorRGBa.YELLOW.mix(ColorRGBa
                                .MAGENTA, k).shade(0.8)
                            gradient.color0 = gradient.color1.shade(1.5)
                            gradient.exponent = 4.0
                            gradient.rotation = 60.0 * ((x + y * 17) % 6)
                            shadeStyle = gradient
                            contour(hexagon)
                        }
                    }
                }
            }
        }
    }
}
