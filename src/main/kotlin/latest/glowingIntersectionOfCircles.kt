package latest

import aBeLibs.data.uniquePairs
import aBeLibs.geometry.intersections
import aBeLibs.random.pickWeighted
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.color.rgb
import org.openrndr.draw.colorBuffer
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.renderTarget
import org.openrndr.extra.fx.blur.GaussianBloom
import org.openrndr.extra.noise.Random
import org.openrndr.extra.noise.gradientPerturbFractal
import org.openrndr.extra.noise.simplex
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.shape.Circle

private data class ColoredCircle(
    val color: ColorRGBa = rgb(listOf("D1313D", "E5625C", "F9BF76", "8EB2C5", "615375").random()),
    var circle: Circle = Circle(Vector2.ZERO,
        listOf(25.0, 50.0, 200.0).pickWeighted(listOf(10.0, 5.0, 1.0))
    )
)

fun main() = application {
    configure {
        width = 720
        height = 721
    }

    program {
        val circles = List(20) { ColoredCircle() }
        val dry = renderTarget(width, height) {
            colorBuffer()
        }
        val wet = colorBuffer(width, height)
        val glow = GaussianBloom().apply {
            window = 14
            sigma = 0.05
            gain = 10.0
        }

        //extend(ScreenRecorder())
        extend {
            drawer.clear(ColorRGBa.BLACK)
            drawer.circles {
                circles.forEachIndexed { i, it ->
                    fill = null
                    stroke = it.color
                    strokeWeight = 2.0
                    val p = gradientPerturbFractal(i, position = Vector2(
                        i * 0.1, seconds * 0.002))
                    val theta = seconds * (i - 4) * 0.4 + simplex(i, p) * 180
                    val radius = simplex(i, p.copy(x = p.x + 0.2)) * 150 + 200
                    it.circle = it.circle.movedTo(
                        drawer.bounds.center + Polar(theta, radius).cartesian
                    )
                    circle(it.circle.scaledTo(it.circle.radius + 2))
                }
            }
            drawer.isolatedWithTarget(dry) {
                clear(ColorRGBa.TRANSPARENT)
                stroke = null
                drawer.circles {
                    circles.uniquePairs().forEach {
                        val (first, second) = it.toList()
                        first.circle.intersections(second.circle).forEach { pos ->
                            fill = ColorRGBa.WHITE.shade(Random.double(0.4, 1.0))
                            circle(pos, 5.0)
                        }
                    }
                }
            }
            glow.apply(dry.colorBuffer(0), wet)
            drawer.image(wet)
        }
    }
}
