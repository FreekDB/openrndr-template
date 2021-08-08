import aBeLibs.extensions.FPSDisplay
import aBeLibs.geometry.randomPoint
import org.openrndr.applicationSynchronous
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.DrawPrimitive
import org.openrndr.draw.shadeStyle
import org.openrndr.draw.vertexBuffer
import org.openrndr.draw.vertexFormat
import org.openrndr.extra.noise.Random
import org.openrndr.extra.noise.simplex3D
import org.openrndr.extra.noise.withVector2Output

// openrndr 0.4
fun main() = applicationSynchronous {
    configure {
        width = 800
        height = 800
    }
    program {
        // create a buffer and specify it's format and size.
        val geometry = vertexBuffer(vertexFormat {
            position(3)
            color(4)
        }, 2 * 210000)

        // create an area with some padding around the edges
        val area = drawer.bounds.offsetEdges(-50.0)

        val n = simplex3D.withVector2Output()
        // populate the vertex buffer.
        geometry.put {
            for (i in 0 until geometry.vertexCount / 2) {
                val p = area.randomPoint()
                write(p.vector3(z = 0.0))
                write(Random.vector4(0.0, 1.0))
                write(
                    (p + n(5, p.x * 0.01, p.y * 0.01, 0.0) * 5.0)
                        .vector3(z = 0.0)
                )
                write(Random.vector4(0.0, 1.0))
            }
        }
        extend(FPSDisplay(color = ColorRGBa.WHITE))
        extend {
            // shader using the color attributes from our buffer
            drawer.shadeStyle = shadeStyle {
                fragmentTransform = "x_fill = va_color;"
            }
            drawer.vertexBuffer(geometry, DrawPrimitive.LINES)
        }
    }
}
