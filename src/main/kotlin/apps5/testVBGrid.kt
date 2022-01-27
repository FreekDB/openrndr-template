package apps5

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.DrawPrimitive
import org.openrndr.extra.shapes.grid
import org.openrndr.extras.meshgenerators.toMesh

/**
 * Simple program to test vertexBuffer rectangles
 */

fun main() = application {
    configure {
        width = 1024
        height = 1024
    }

    program {
        extend {
            drawer.clear(ColorRGBa.GRAY)
            drawer.fill = ColorRGBa.PINK
            drawer.stroke = null
            drawer.bounds.grid(8, 6, 50.0, 50.0, 8.0, 8.0).flatten().forEach {
                val mesh = it.toMesh(2.0)
                drawer.vertexBuffer(mesh, DrawPrimitive.TRIANGLES)
                mesh.destroy()
            }
        }
    }
}