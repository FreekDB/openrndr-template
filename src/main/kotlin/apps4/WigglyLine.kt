package apps4

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.DrawPrimitive
import org.openrndr.draw.shadeStyle
import org.openrndr.draw.vertexBuffer
import org.openrndr.draw.vertexFormat
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.Random
import org.openrndr.math.Polar
import org.openrndr.shape.ShapeContour

fun main() = application {
    configure {
        width = 800
        height = 800
    }
    program {
         val geometry = vertexBuffer(vertexFormat {
            position(3)
            color(4)
        }, 100)

        extend(Screenshots())
        extend {
            drawer.clear(ColorRGBa.WHITE)
            drawer.shadeStyle = shadeStyle {
                fragmentTransform = "x_fill = va_color;"
            }
            val line = ShapeContour.fromPoints(List(geometry.vertexCount / 2) {
                val theta = it * 10.0 + seconds * 20
                val radius = 200.0 + 80 * Random.perlin(theta * 0.01, 1.0)
                Polar(theta, radius).cartesian + drawer.bounds.center
            }, false)

//            val texturedLine = ColoredLineMesh(
//                vertexFormat {
//                    position(3)
//                    color(4)
//                }, shadeStyle {
//                    fragmentTransform = "x_fill = va_color;"
//                }, 100
//            )
//            texturedLine.draw(
//                drawer, line,
//                color = { pc, pos ->
//                    ColorRGBa.GRAY.mix(ColorRGBa.PINK, pc).toVector4()
//                },
//                width = { pc, pos ->
//                    40 + 20 * Random.perlin(pos * 0.01)
//                },
//            )

            val points = geometry.vertexCount / 2
            geometry.put {
                for (i in 0 until points) {
                    val pc = i / (points - 1.0)
                    val color =
                        ColorRGBa.GRAY.mix(ColorRGBa.PINK, pc).toVector4()
                    val pos = line.position(pc)
                    val normal = line.normal(pc).normalized *
                            (40 + 20 * Random.perlin(pos * 0.01))
                    write((pos + normal).vector3(z = 0.0))
                    write(color)
                    write((pos - normal).vector3(z = 0.0))
                    write(color)
                }
            }
            drawer.vertexBuffer(geometry, DrawPrimitive.TRIANGLE_STRIP)
        }
    }
}