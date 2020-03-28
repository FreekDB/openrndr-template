import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.color.mix
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.shadestyles.linearGradient
import org.openrndr.math.IntVector2
import org.openrndr.math.Polar
import org.openrndr.shape.SegmentJoin
import org.openrndr.shape.ShapeContour
import kotlin.system.exitProcess

/**
 * Testing recent changes to ShapeContour and .offset()
 */

fun main() = application {
    configure {
        width = 600
        height = 600
        hideWindowDecorations = true
    }

    program {
        val curves = mutableListOf<ShapeContour>()

        // Make the core shape
        curves.add(
            ShapeContour.fromPoints(
                List(5) {
                    Math.random() * 360
                }.sorted().map { angle ->
                    Polar(angle, 100.0).cartesian
                }, true
            )
        )

        // Make offset shapes, each based on the previous one
        for (i in 0..6) {
            curves.add(curves[i].offset(-5.0 - i * 7.0, SegmentJoin.BEVEL))
        }

        extend(Screenshots())

        extend {
            drawer.background(ColorRGBa.fromHex(0x355C7D))
            drawer.stroke = ColorRGBa.BLACK.opacify(0.1)
            drawer.translate(width * 0.5, height * 0.5)

            // Draw first larger shapes, then smaller ones
            for (i in curves.size - 1 downTo 0) {
                val percent = i / (curves.size - 1.0)
                val a = mix(
                    ColorRGBa.fromHex(0xF8B195),
                    ColorRGBa.fromHex(0xF67280),
                    percent
                )
                val b = mix(
                    ColorRGBa.fromHex(0xC06C84),
                    ColorRGBa.fromHex(0x6C5B7B),
                    percent
                )
                drawer.shadeStyle = linearGradient(a, b, rotation = i * 185.0)
                drawer.contour(curves[i])
            }
        }

        keyboard.keyDown.listen {
            if (it.key == KEY_ESCAPE) {
                exitProcess(0)
            }
        }

    }
}
