package apps2022

import org.openrndr.KEY_ENTER
import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.color.rgb
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.color.spaces.toOKLABa
import org.openrndr.extra.noise.Random
import org.openrndr.extra.shadestyles.radialGradient
import org.openrndr.shape.LineSegment
import org.openrndr.shape.Rectangle
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.intersections
import kotlin.math.max

fun main() = application {
    configure {
        width = 1500
        height = 1200
    }

    program {
        val contours = mutableListOf<ShapeContour>()

        fun doit() {
            contours.clear()
            contours.add(
                Rectangle.fromCenter(drawer.bounds.center, 220.0, 220.0).contour
            )
            repeat(300) {
                val c = contours.takeLast(20).random()
                val sz = max(c.bounds.width, c.bounds.height)
                val t = Random.int0(8) / 8.0
                val n = c.normal(t)
                val p = c.position(t)
                val len = 0.2 + 0.1 * Random.int0(4)
                val scale = 0.5 + 0.3 * Random.int0(3)
                val dist = scale / 2 + len
                val line = LineSegment(p + n * 0.05, p + n * sz * len).contour
                val center = p + n * sz * dist
                val side = sz * scale
                val rect = Rectangle.fromCenter(center, side).contour
                val margin = Rectangle.fromCenter(center, side + 10).contour
                if (contours.all {
                        it.intersections(line).isEmpty() &&
                                it.intersections(margin).isEmpty() &&
                                drawer.bounds.contour.nearest(center).position.distanceTo(center) > side
                    }) {
                    contours.add(line)
                    contours.add(rect)
                }
            }
        }

        val gradient = radialGradient(
            ColorRGBa.WHITE.toOKLABa(),
            ColorRGBa.BLACK.toOKLABa()
        )
        val bg = radialGradient(
            rgb(0.404, 0.620, 0.678).toOKLABa(),
            rgb(0.729, 0.788, 0.713).toOKLABa()
        )

        extend(Screenshots())
        extend {
            drawer.shadeStyle = bg
            drawer.stroke = null
            drawer.rectangle(drawer.bounds)

            drawer.shadeStyle = gradient
            gradient.color0 = rgb(0.992, 0.918, 0.671).toOKLABa()
            gradient.color1 = rgb(0.220, 0.165, 0.259).toOKLABa()
            drawer.stroke = ColorRGBa.BLACK.opacify(0.7)
            contours.forEach {
                gradient.offset = (it.bounds.center - drawer.bounds.center) * 0.002
                drawer.contour(it)
            }
        }

        keyboard.keyDown.listen {
            when (it.key) {
                KEY_ENTER -> doit()
                KEY_ESCAPE -> application.exit()
            }
        }
    }
}