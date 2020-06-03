package apps.simpleTests

import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.Random
import org.openrndr.math.IntVector2
import org.openrndr.math.Vector2
import org.openrndr.shape.SegmentJoin
import org.openrndr.shape.ShapeContour
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.system.exitProcess

/**
 * Testing recent changes to ShapeContour and .offset()
 */

fun main() = application {
    configure {
        width = 600
        height = 600
        hideWindowDecorations = true
        position = IntVector2(10, 10)
    }

    program {
        val curves = mutableListOf<ShapeContour>()

        curves.add(
            ShapeContour.fromPoints(List(80) {
                val a = 2 * PI * it / 80.0
                Vector2(cos(a), sin(a)) * (100.0 + 20.0 * Random.simplex(cos(a), sin(a)))
            }, true)
        )

        for (i in 0..6) {
            curves.add(curves[i].offset(-20.0, SegmentJoin.MITER))
        }

        extend(Screenshots())

        extend {
            drawer.background(ColorRGBa.WHITE)
            drawer.stroke = ColorRGBa(0.0, 0.0, 0.0, 0.05)
            drawer.translate(width * 0.5, height * 0.5)
            for (i in curves.size - 1 downTo 0) {
                drawer.fill = ColorRGBa(i * 0.15, 0.7, 1.0 - i * 0.18, 0.1)
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
