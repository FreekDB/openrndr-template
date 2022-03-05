package apps.p5

import aBeLibs.random.pickWeighted
import org.openrndr.application
import org.openrndr.color.hsl
import org.openrndr.color.rgb
import org.openrndr.draw.circleBatch
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.Random
import org.openrndr.shape.Circle

/**
 * id: b20fc191-27fa-494d-8e75-d69e2a5cebe6
 * description: example from
 * https://discourse.processing.org/t/writing-processing-in-kotlin/3957
 * tags: #new
 */

private fun Circle.overlaps(other: Circle) =
    center.distanceTo(other.center) < radius + other.radius

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

        val circleList = mutableListOf<Circle>()
        val colors = listOf(
            rgb(Random.double(0.9, 1.0)),
            hsl(Random.double(180.0, 220.0), 0.5, 0.25).toRGBa(),
            hsl(Random.double0(20.0), 0.8, 0.4).toRGBa()
        )
        val colorWeights = listOf(0.6, 0.3, 0.1)

        // Batches are more efficient than drawing circles one by one, as
        // they get sent all together to the GPU
        val circleBatch = drawer.circleBatch {
            for ((circleRadius, circleCount) in circleSizeCounts) {
                for (i in 1..circleCount) {
                    // allow up to 100 collisions
                    for (c in 0..99) {
                        val newCircle = Circle(
                            Random.point(drawer.bounds.offsetEdges(-circleRadius)),
                            circleRadius
                        )
                        if (circleList.none { it.overlaps(newCircle) }) {
                            stroke = null
                            fill = colors.pickWeighted(colorWeights)
                            circle(newCircle)
                            circleList.add(newCircle)
                            break
                        }
                    }
                }
            }
        }

        extend(Screenshots())
        extend {
            drawer.clear(rgb(0.27))
            drawer.circles(circleBatch)
        }
    }
}
