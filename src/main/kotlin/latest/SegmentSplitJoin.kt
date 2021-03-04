package latest

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.color.rgb
import org.openrndr.draw.LineJoin
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.Random
import org.openrndr.extra.shadestyles.RadialGradient
import org.openrndr.extra.shadestyles.radialGradient
import org.openrndr.extras.color.presets.CORNFLOWER_BLUE
import org.openrndr.extras.color.presets.LAVENDER
import org.openrndr.extras.color.presets.ORCHID
import org.openrndr.math.Polar
import org.openrndr.shape.Segment
import org.openrndr.shape.draw
import org.openrndr.shape.drawComposition

fun main() = application {
    configure { width = 1024; height = 1024 }

    program {
        val design = drawComposition { }

        val gradient = radialGradient(
            rgb("312f44"), rgb("22212f")
        )

        fun newDesign() {
            design.clear()
            repeat(20) {
                val center = drawer.bounds.center
                val dir = Polar(360 * it / 20.0).cartesian
                val randomSegment = Segment(
                    center + dir * 100.0,
                    center + dir * 500.0,
                    center + Polar(Random.double0(360.0), 300.0).cartesian,
                    center + Polar(Random.double0(360.0), 500.0).cartesian
                )
                val split = randomSegment.split(Random.double0())
                val joined = join(split[0], split[1])
                design.draw {
                    strokeWeight = 10.0
                    stroke = ColorRGBa.WHITE.shade(0.8)
                    contour(joined.contour)
                    //segment() is not yet implemented in CompositionDrawer

                    strokeWeight = 4.0
                    stroke = ColorRGBa.RED
                    contour(split[0].contour)
                    stroke = ColorRGBa.CORNFLOWER_BLUE
                    contour(split[1].contour)

                    stroke = ColorRGBa.BLACK
                    strokeWeight = 1.0
                    contour(randomSegment.contour)
                }
            }
        }
        extend(Screenshots())
        extend {
            drawer.apply {
                shadeStyle = gradient
                rectangle(drawer.bounds)
                lineJoin = LineJoin.ROUND
                composition(design)

            }
        }
        mouse.buttonDown.listen {
            newDesign()
        }
    }
}

private fun join(a: Segment, b: Segment): Segment {
    val k = (b.control[0] - b.start).length / (a.end - a.control[1]).length
    val c0 = a.control[0] * (1 + k) - a.start * k
    val c1 = (b.control[1] * (1 + k) - b.end) / k
    return Segment(a.start, c0, c1, b.end)
}
