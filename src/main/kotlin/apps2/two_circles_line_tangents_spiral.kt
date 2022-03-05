package apps2

import aBeLibs.geometry.spiralContour
import aBeLibs.geometry.tangentWrapConcave
import aBeLibs.math.TAU
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.math.Polar
import org.openrndr.shape.Circle
import java.lang.Math.toDegrees
import kotlin.math.sin

/**
 * id: 04849610-ff0f-4e19-aafa-18f3cd2bc2ec
 * description: New sketch
 * tags: #new
 */

fun main() = application {
    configure {
        width = 512
        height = 512
    }
    program {
        val makeGif = false

        extend {
            val pc = TAU * (frameCount % 120) / 120.0
            drawer.run {
                clear(ColorRGBa.fromHex("EEEFAC"))
                fill = null

                val r = width * 0.01
                val p0 = bounds.position(0.28, 0.28) + Polar(
                    toDegrees(pc + 1),
                    r
                ).cartesian
                val p1 = bounds.position(0.6, 0.6) + Polar(
                    toDegrees(pc + 4),
                    r
                ).cartesian

                val c = listOf(
                    Circle(p0, width * 0.15 + sin(pc, 10)),
                    Circle(p1, width * 0.3 + sin(pc + 2, r * 2)),
                    Circle(p0, width * 0.05 + sin(pc + 1, r)),
                    Circle(p1, width * 0.12 + sin(pc + 3, r))
                )

                stroke = ColorRGBa.fromHex("cdc68a")
                strokeWeight = 1.0
                circles(c)

                stroke = ColorRGBa.fromHex("41A97F")
                strokeWeight = 4.0
                val wrap0 =
                    tangentWrapConcave(c[0], c[1], 250 + sin(pc + 2, r * 20))
                segment(wrap0.segments[0])

                val wrap1 =
                    tangentWrapConcave(c[2], c[3], 250 + sin(pc + 5, r * 20))
                segment(wrap1.segments[4])

                val spiral0 = spiralContour(
                    wrap0.segments[0].start,
                    wrap1.segments[4].end,
                    c[0].center,
                    -3
                )
                contour(spiral0)

                val spiral1 = spiralContour(
                    wrap1.segments[4].start,
                    wrap0.segments[0].end,
                    c[1].center,
                    -3
                )
                contour(spiral1)

                stroke = ColorRGBa.fromHex("FE7008")
                circles(
                    listOf(
                        wrap0.segments[0].start, wrap0.segments[0].end,
                        wrap1.segments[4].start, wrap1.segments[4].end
                    ), 5.0
                )
            }

            @Suppress("ConstantConditionIf")
            if (makeGif) {
                if (frameCount == 120) {
                    application.exit()
                }
            }
        }
    }

}

private fun sin(radians: Double, dist: Double) = dist * sin(radians)
private fun sin(radians: Double, dist: Int) = dist * sin(radians)