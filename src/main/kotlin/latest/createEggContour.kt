package latest

import aBeLibs.random.pickWeighted
import aBeLibs.geometry.circleish
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.color.rgb
import org.openrndr.draw.isolated
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.Random
import org.openrndr.extra.shadestyles.RadialGradient
import org.openrndr.extra.shadestyles.radialGradient
import org.openrndr.extras.color.presets.LAVENDER
import org.openrndr.extras.color.presets.ORCHID
import org.openrndr.math.Vector2
import org.openrndr.math.transforms.transform
import org.openrndr.shape.Rectangle
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.contains

/**
 * Falling potatoes find a good place to rest on top of other potatoes.
 */
private val ShapeContour.center: Vector2
    get() = segments.map { it.start }
        .reduce { sum, v -> sum + v } / segments.size.toDouble()

fun main() = application {
    program {
        Random.seed = System.currentTimeMillis().toString()
        val xPositions = (0 until width).map { it * 1.0 }.shuffled()
        val participants = mutableListOf(
            Rectangle(0.0, height + 1.0, width * 1.0, 20.0).contour,
            Rectangle(0.0, 0.0, -100.0, height * 1.0).contour,
            Rectangle(width + 1.0, 0.0, 100.0, height * 1.0).contour,
        )
        val gradient: RadialGradient = radialGradient(
            ColorRGBa.ORCHID, ColorRGBa.LAVENDER
        )

        fun makeShape() = circleish(
            Vector2.ZERO,
            listOf(20.0, 40.0, 80.0, 120.0).pickWeighted(
                listOf(10.0, 5.0, 2.0, 1.0)
            ),
            Random.double0(360.0)
        )

        var nextShape = makeShape()
        var pos = Vector2.ZERO
        var y = 0.0

        fun update() {
            y++
            val x = xPositions.firstOrNull { x ->
                val moved = nextShape.transform(transform {
                    translate(x, y)
                })
                participants.none { it.overlaps(moved) }
            }
            if (x == null) {
                val moved = nextShape.transform(transform {
                    translate(pos)
                })

                //participants.add(moved.offset(-2.0))
                participants.add(moved)
                nextShape = makeShape()
                pos = Vector2.ZERO
                y = 0.0
            } else {
                pos = Vector2(x, y)
            }
        }

        extend(Screenshots())
        extend {
            drawer.isolated {
                gradient.color0 = rgb("DDFFF7")
                gradient.color1 = rgb("93E1D8")
                gradient.exponent = 1.0
                gradient.length = 0.5
                shadeStyle = gradient
                stroke = null
                rectangle(bounds)
            }
            drawer.isolated {
                gradient.color0 = rgb("AA4465")
                gradient.color1 = rgb("FFA69E")
                gradient.exponent = 3.0
                gradient.length = 1.0
                shadeStyle = gradient
                stroke = null
                List(10) { update() }
                drawer.contour(nextShape.transform(transform {
                    translate(pos)
                }))
                contours(participants)
            }
        }
        keyboard.keyDown.listen {
            val n = participants.size
            when (it.name) {
                "escape" -> application.exit()
            }
        }
    }
}

private fun ShapeContour.overlaps(other: ShapeContour) =
    this.bounds.intersects(other.bounds) && (
            this.bounds.contains(other.center) ||
                    other.bounds.contains(this.center) ||
                    other.contains(this.nearest(other.center).position)
                    // TODO: I wanted to use .intersections instead of this
                    // hack but it needs to be fixed first in artifex
            )
