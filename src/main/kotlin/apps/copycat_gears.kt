package apps

import aBeLibs.lang.loopRepeat
import aBeLibs.math.TAU
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolated
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.shape.contour
import kotlin.math.sin

/**
 * Port of
 * https://github.com/hamoid/Fun-Programming/blob/master/processing/ideas/2017/10/copycat_gears/Gear.pde
 */

fun main() = application {
    configure {
        width = 500
        height = 500
    }

    program {

        class Gear(
            teeth: Int,
            clockwise: Boolean = true,
            shift: Boolean = false,
            var color: ColorRGBa = ColorRGBa.fromHex("B5DE5A")
        ) {
            var pos = Vector2.ZERO
            private var radius = teeth * 10.0
            private val interlocking = 30.0
            private val interlockingLength = TAU * radius
            val iRadius
                get() = radius - interlocking / 2.1
            private val aDelta = 90.0 / teeth
            private var rotationAngle = 0.0
            private val cw = if (clockwise) 1.0 else -1.0
            private var s = contour {
                loopRepeat(teeth, to = 360.0) { theta ->
                    val a = theta +
                            (if (clockwise) aDelta else 0.0) +
                            (if (shift) aDelta * 2.0 else 0.0)
                    moveOrLineTo(Polar(a + aDelta * 0, radius).cartesian)
                    moveOrLineTo(Polar(a + aDelta * 1, radius).cartesian)
                    moveOrLineTo(
                        Polar(
                            a + aDelta * 2,
                            radius - interlocking
                        ).cartesian
                    )
                    moveOrLineTo(
                        Polar(
                            a + aDelta * 3,
                            radius - interlocking
                        ).cartesian
                    )
                }
                close()
            }

            fun rot(len: Double) {
                rotationAngle += cw * 360 * len / interlockingLength
            }

            fun draw() {
                drawer.isolated {
                    fill = color
                    stroke = null
                    translate(pos)
                    rotate(rotationAngle)
                    contour(s)
                }
            }
        }

        var a = 0.0
        val gears = listOf(
            Gear(10, clockwise = false, color = ColorRGBa.fromHex("ecff9d")),
            Gear(5, shift = true, color = ColorRGBa.fromHex("729ab6")),
            Gear(7, color = ColorRGBa.fromHex("f398c5")),
            Gear(6, color = ColorRGBa.fromHex("805085")),
            Gear(8, shift = true, color = ColorRGBa.fromHex("ff3b8f"))
        )

        gears[0].pos = drawer.bounds.center
        gears[1].pos = drawer.bounds.center + Vector2(
            gears[0].iRadius + gears[1].iRadius,
            0.0
        )
        gears[2].pos = drawer.bounds.center + Vector2(
            -gears[0].iRadius - gears[2].iRadius,
            0.0
        )
        gears[3].pos = drawer.bounds.center + Vector2(
            0.0,
            gears[0].iRadius + gears[3].iRadius
        )
        gears[4].pos = drawer.bounds.center + Vector2(
            0.0,
            -gears[0].iRadius - gears[4].iRadius
        )

        extend {
            drawer.run {
                clear(ColorRGBa.fromHex("4d3e53"))
                translate(bounds.position(0.55, 0.55))
                rotate(Math.toDegrees(0.4 * sin(a * 2)))
                scale(0.8 + 0.4 * sin(a))
                translate(-bounds.center)
                gears.forEach { g ->
                    // TODO: calculate this magic number
                    g.rot(4.1887855 + 0.5 * sin(a * 5))
                    g.draw()
                }
                a += TAU / 150
            }
        }
    }
}
