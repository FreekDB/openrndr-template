package apps4

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.math.Vector2
import org.openrndr.shape.ShapeContour
import kotlin.math.sin

/**
 * id: 6dda61b9-375c-4cf2-8b26-f74fb27609dd
 * description: New sketch
 * tags: #new
 */

fun main() = application {
    configure {
        width = 400
        height = 400
    }
    program {
        val thickLine = ThickLine(
            200, ColorRGBa.WHITE, ColorRGBa.GRAY
        ) { t -> 15.0 - t * 15.0 }

//        extend(ScreenRecorder().also { it.profile = GIFProfile() })
        extend {
            drawer.clear(ColorRGBa.WHITE)

            val thinLine = ShapeContour.fromPoints(List(50) {
                val a = seconds * 2 + it * 0.08
                Vector2(
                    sin(a + sin(a * 0.31)),
                    sin(a * 0.53 + sin(a * 0.73))
                ) * 160.0 + drawer.bounds.center
            }, false)

            thickLine.draw(drawer, thinLine)
        }
    }
}

/**
 * A class to draw [ShapeContour] objects as a thick line. The color is
 * linearly interpolated from [color0] to [color1] from line start to end.
 * The thickness of the line is defined by a function that takes a normalized
 * `t` Double value as input and outputs the desired width. Get a uniform width
 * using something like `{ t -> 10.0 }`
 */
class ThickLine(
    private val pointCount: Int,
    color0: ColorRGBa,
    color1: ColorRGBa,
    val width: (Double) -> Double
) {
    val geometry = vertexBuffer(vertexFormat {
        position(3)
        attribute("t", VertexElementType.FLOAT32)
    }, pointCount * 2)

    val style = shadeStyle {
        fragmentTransform = "x_fill = mix(p_color0, p_color1, va_t);"
        parameter("color0", color0)
        parameter("color1", color1)
    }

    fun draw(drawer: Drawer, thinLine: ShapeContour) {
        drawer.shadeStyle = style
        geometry.put {
            for (i in 0 until pointCount) {
                val pc = i / (pointCount - 1.0)
                val pos = thinLine.position(pc)
                val normal = thinLine.normal(pc).normalized * width(pc) / 2.0
                write((pos + normal).vector3(z = 0.0))
                write(pc.toFloat())
                write((pos - normal).vector3(z = 0.0))
                write(pc.toFloat())
            }
        }
        drawer.vertexBuffer(geometry, DrawPrimitive.TRIANGLE_STRIP)
    }
}
