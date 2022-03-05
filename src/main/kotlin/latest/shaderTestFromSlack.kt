package latest

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.shadeStyle
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.shape.LineSegment

/**
 * id: 88f419d6-543d-4a65-8350-97b7ec9dfc42
 * description: New sketch
 * tags: #new
 */

fun main() = application {
    configure {
        width = 500
        height = 500
    }

    program {
        extend {
            val fx = shadeStyle {
                fragmentTransform = """
                        float t = c_contourPosition / p_len;
                        x_stroke.rgb = vec3(0.5 + 0.5 * sin(t * 93.3) * sin(t * 67.3));
                    """.trimIndent()
            }
            drawer.apply {
                val seg = LineSegment(mouse.position, bounds.center)
                clear(ColorRGBa.WHITE)
                fill = null
                shadeStyle = fx
                fx.parameter("len", seg.start.distanceTo(seg.end))
                lineSegment(seg)

                val r = 200.0
                fx.parameter("len", r)
                translate(bounds.center)
                rotate(seconds * 10)
                lineLoop(List(4) { Polar(it * 90.0, r).cartesian })
                // not affected
                rectangle(Vector2.ZERO, r / 2, r / 2)
                circle(Vector2.ZERO, r / 2)
            }
        }
    }
}
