import aBeLibs.geometry.pillShape
import com.soywiz.korma.random.randomWithWeights
import org.openrndr.KEY_ENTER
import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.renderTarget
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.Random
import org.openrndr.extra.noise.random
import org.openrndr.extra.noise.uniform
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.shape.*

fun main() = application {
    configure {
        width = 1000
        height = 1000
    }
    program {

        var center = Vector2.ZERO
        val rt = renderTarget(width, height) {
            colorBuffer()
            depthBuffer()
        }
        val colors = listOf(ColorRGBa.WHITE, ColorRGBa.BLACK)

        fun nu() {
            // Populate [segments] (the skeleton of the shape)
            val spine = mutableListOf<Segment>()
            while (spine.size < 10) {
                if (spine.isEmpty()) {
                    val start = drawer.bounds.uniform(150.0)
                    val end = drawer.bounds.uniform(150.0)
                    if (start.distanceTo(end) > 80.0) {
                        spine.add(Segment(start, end))
                    }
                } else {
                    val okArea = drawer.bounds.offsetEdges(-150.0)
                    val start = spine
                        .randomWithWeights(List(spine.size) { i -> 1.0 + i * i })
                        .position(listOf(0.0, 1.0).random())
                    val end = (start + Polar(
                        Random.double0(360.0),
                        Random.double(85.0, 250.0)
                    ).cartesian).clamp(okArea)
                    val segNew = Segment(start, end)
                    if (spine.all { segOld ->
                            segOld.nearest(end).position.distanceTo(end) > 80.0 &&
                                    segOld.intersections(segNew).none {
                                        it.b.segmentT > 0
                                    }
                        }) {
                        spine.add(segNew)
                    }
                }
            }

            // Find center of the segments
            center = spine.map { it.bounds }.bounds.center

            drawer.isolatedWithTarget(rt) {
                clear(colors.first())
                stroke = null

                // Circles
                spine.forEachIndexed { i, it ->
                    fill = colors[i % 2]
                    stroke = colors[(i + 1) % 2]
                    strokeWeight = random(5.0, 20.0)
                    circle(it.start, 120.0 - i * 8)
                    strokeWeight = random(5.0, 20.0)
                    circle(it.end, 110.0 - i * 8)
                }

                // Connected circles
                spine.forEachIndexed { i, it ->
                    fill = colors[(i + 1) % 2]
                    stroke = null
                    val c0 = Circle(it.start, 100.0 - i * 8)
                    val c1 = Circle(it.end, 90.0 - i * 8)
                    contour(pillShape(c0, c1))

                    fill = colors[i % 2]
                    contour(
                        pillShape(
                        c0.copy(radius = c0.radius * 0.9),
                        c1.copy(radius = c1.radius * 0.9)
                    )
                    )
                }

                // Axis circles
                spine.forEachIndexed { i, it ->
                    fill = colors[(i+1) % 2]
                    circle(it.start, 60.0 - i * 4)
                    circle(it.end, 40.0 - i * 4)
                }
            }
        }
        nu()

        extend(Screenshots())
        extend {
            drawer.clear(colors.first())
            drawer.translate(drawer.bounds.center - center)
            drawer.image(rt.colorBuffer(0))
        }
        keyboard.keyDown.listen {
            when (it.key) {
                KEY_ENTER -> nu()
                KEY_ESCAPE -> application.exit()
            }
        }
    }
}

