package apps.p5

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.color.hsl
import org.openrndr.color.rgb
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.Random
import org.openrndr.math.Vector2
import org.openrndr.shape.Circle
import aBeLibs.random.pickWeighted

/**
 * example from
 * https://discourse.processing.org/t/writing-processing-in-kotlin/3957
 */

data class ColorCircle(val circle: Circle, val color: ColorRGBa)

fun main() = application {
    configure {
        width = 500
        height = 500
    }

    program {
        val circleSizeCounts = listOf(
            65.0 to 19,
            37.0 to 38,
            20.0 to 75,
            7.0 to 150,
            3.0 to 300
        )

        val circles = mutableListOf<ColorCircle>()

        for ((circleRadius, circleCount) in circleSizeCounts) {
            for (i in 1..circleCount) {
                // allow up to 100 collisions
                for (c in 0..1000) {
                    // generate random point
                    // do not allow circles to overlap canvas
                    val circlePosition = Vector2(
                        Random.double(circleRadius, width - circleRadius),
                        Random.double(circleRadius, height - circleRadius)
                    )
                    // allow circles overlapping canvas
                    //val circlePosition = Random.Vector2() * drawer.bounds.center + drawer.bounds.center
                    val newCircle = Circle(circlePosition, circleRadius)
                    if (circles.all { newCircle.center.distanceTo(it.circle.center) > it.circle.radius + newCircle.radius }) {
                        // get random color
                        val color = listOf(
                            rgb(Random.double(0.9, 1.0)),
                            hsl(Random.double(180.0, 220.0), 0.5, 0.25).toRGBa(),
                            hsl(Random.double0(20.0), 0.8, 0.4).toRGBa()
                        ).pickWeighted(
                            listOf(0.6, 0.3, 0.1)
                        )
                        circles.add(ColorCircle(newCircle, color))
                        break
                    }
                }
            }
        }

        extend(Screenshots())
        extend {
            drawer.run {
                clear(rgb(0.27))
                stroke = null
                strokeWeight = 0.0
                circles.forEach {
                    fill = it.color
                    circle(it.circle)
                }
            }
        }
    }
}
