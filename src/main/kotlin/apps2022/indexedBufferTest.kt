package apps2022

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.extra.noise.Random
import org.openrndr.math.Vector3
import org.openrndr.math.Vector4
import org.openrndr.math.transforms.transform

/**
 * id: c1d2df7e-bf58-4a6f-a4ba-c433d4f26f5f
 * description: Figure out how to add a `color` to the `transforms`
 * buffer so each shape has a unique color, accessible as `vi_color`
 * in the fragment shader.
 * tags: #new
 */

fun main() = application {
    configure { }
    program {
        // -- create the vertex buffer
        val geometry = vertexBuffer(vertexFormat {
            position(3)
        }, 4)

        // -- fill the vertex buffer with vertices for a unit quad
        geometry.put {
            write(Vector3(-1.0, -1.0, 0.0))
            write(Vector3(-1.0, 1.0, 0.0))
            write(Vector3(1.0, -1.0, 0.0))
            write(Vector3(1.0, 1.0, 0.0))
        }

        // -- create the secondary vertex buffer, which will hold transformations
        val transforms = vertexBuffer(vertexFormat {
            attribute("transform", VertexElementType.MATRIX44_FLOAT32)
            color(4)
        }, 1000)

        // -- fill the transform buffer
        transforms.put {
            for (i in 0 until 1000) {
                write(transform {
                    translate(Math.random() * width, Math.random() * height)
                    rotate(Vector3.UNIT_Z, Math.random() * 360.0)
                    scale(Math.random() * 30.0)
                })
                write(
                    Vector4(
                        Random.double0(),
                        Random.double0(),
                        Random.double0(),
                        Random.double0()
                    )
                )
            }
        }
        extend {
            drawer.fill = ColorRGBa.PINK.opacify(0.25)
            drawer.shadeStyle = shadeStyle {
                vertexTransform = "x_viewMatrix = x_viewMatrix * i_transform;"
                fragmentTransform = "x_fill = vi_color;"
            }
            drawer.vertexBufferInstances(
                listOf(geometry),
                listOf(transforms),
                DrawPrimitive.TRIANGLE_STRIP,
                1000
            )
        }
    }
}
