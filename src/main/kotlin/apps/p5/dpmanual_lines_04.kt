package apps.p5

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.noise.Random
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.math.map

/**
 * id: 18cfdcc0-5245-458d-b323-d3d6cb608673
 * description: New sketch
 * tags: #new
 */

// activate "orx-noise" in build.gradle.kts

fun main() = application {
    configure {
        width = 500
        height = 500
    }

    program {

        val lenMax = 180.0
        var randSeed = "1"

        keyboard.character.listen {
            when (it.character) {
                'r' -> randSeed = Math.random().toString()
            }
        }

        extend {
            Random.seed = randSeed
            drawer.clear(ColorRGBa.BLACK)
            drawer.stroke = ColorRGBa.WHITE
            drawer.translate(drawer.bounds.center)

            val numRepeats = mouse.position.x.map(0.0, width * 1.0, 5.0, 150.0, true).toInt()
            val thickness = mouse.position.y.map(0.0, height * 1.0, 0.25, 25.0)
            drawer.strokeWeight = thickness

            val lines = mutableListOf<Vector2>()
            (0 until numRepeats).forEach {
                val len = Random.double0(lenMax)
                val angle = it * 360.0 / numRepeats
                lines.add(Polar(angle, 50.0).cartesian)
                lines.add(Polar(angle, 50.0 + len).cartesian)
            }
            drawer.lineSegments(lines)
        }
    }
}
