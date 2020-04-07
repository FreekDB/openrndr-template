package p5

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.noise.valueLinear
import org.openrndr.math.Polar
import org.openrndr.math.map

// activate "orx-noise" in build.grtadle.kts

// From online book by Mark

fun main() = application {
    configure {
        width = 500
        height = 500
    }

    program {

        val lenMax = 180
        var randSeed = 1

        keyboard.character.listen {
            when (it.character) {
                'r' -> randSeed = (Math.random() * 1000).toInt()
            }
        }

        extend {
            drawer.background(ColorRGBa.BLACK)
            drawer.stroke = ColorRGBa.WHITE
            drawer.translate(drawer.bounds.center)

            val numRepeats = mouse.position.x.map(0.0, width.toDouble(), 5.0, 150.0, true).toInt()
            val thickness = mouse.position.y.map(0.0, height.toDouble(), 0.25, 25.0)
            drawer.strokeWeight = thickness

            (0 until numRepeats).forEach {
                val degree = 360.0 / it
                val len = valueLinear(randSeed, degree, 0.0) * 0.5 + 0.5
                drawer.lineSegment(
                    Polar(degree, 50.0).cartesian,
                    Polar(degree, 50.0 + len * lenMax).cartesian
                )
            }
        }
    }
}