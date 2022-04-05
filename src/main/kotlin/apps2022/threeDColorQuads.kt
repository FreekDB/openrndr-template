package apps2022

import apps.p5.Quad
import org.openrndr.WindowMultisample
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.extra.noise.Random
import org.openrndr.extras.camera.Orbital
import org.openrndr.extras.color.presets.SEA_GREEN
import org.openrndr.ffmpeg.ScreenRecorder
import org.openrndr.math.Vector3
import org.openrndr.math.transforms.transform

/**
 * id: c8f7f3db-c1cf-4336-b1d4-a58c33e0addf
 * description: Shows how to render a list of Quads as shaded triangles
 * and edges. Can be used to create designs based on 3D Quads and render them.
 * tags: #3D #Quad
 */

data class ColorQuad(
    val a: Vector3, val b: Vector3, val c: Vector3, val d: Vector3,
    val aCol: ColorRGBa, val bCol: ColorRGBa, val cCol: ColorRGBa, val dCol: ColorRGBa
)

fun main() = application {
    configure {
        width = 400
        height = 400
        multisample = WindowMultisample.SampleCount(8)
    }
    program {
        val cam = Orbital()
        cam.eye = -Vector3.UNIT_Z * 150.0

//        val quads = List(50) {
//            val orientation = listOf(
//                Vector3.UNIT_Z, Vector3.UNIT_X, Vector3.UNIT_Y
//            ).shuffled().take(2).map {
//                it * Random.sign()
//            }
//            val a = Vector3.uniform(-50.0, 50.0)
//            val dx = orientation[0] * 20.0
//            val dy = orientation[1] * 20.0
//            ColorQuad(
//                a, a + dx, a + dx + dy, a + dy,
//                ColorRGBa.random(), ColorRGBa.random(), ColorRGBa.random(), ColorRGBa.random()
//            )
//        }
        // These don't need to be Quads. Could be 3D polylines instead.
        val crossSections = List(30) {
            val a = Vector3(-10.0, -10.0, 0.0).xyz1
            val b = Vector3(-10.0, 10.0, 0.0).xyz1
            val c = Vector3(10.0, 10.0, 0.0).xyz1
            val d = Vector3(10.0, -10.0, 0.0).xyz1

            val m = transform {
                rotate(Vector3.UNIT_Y, it * 12.0)
                translate(50.0, 0.0, 0.0)
            }

            Quad((m * a).xyz, (m * b).xyz, (m * c).xyz, (m * d).xyz)
        }

        val quads = (crossSections + crossSections.first()).zipWithNext { a, b ->
            listOf(
                ColorQuad(
                    a.a, a.b, b.b, b.a,
                    ColorRGBa.random(), ColorRGBa.random(), ColorRGBa.random(), ColorRGBa.random()
                ),
                ColorQuad(
                    a.b, a.c, b.c, b.b,
                    ColorRGBa.random(), ColorRGBa.random(), ColorRGBa.random(), ColorRGBa.random()
                ),
                ColorQuad(
                    a.c, a.d, b.d, b.c,
                    ColorRGBa.random(), ColorRGBa.random(), ColorRGBa.random(), ColorRGBa.random()
                ),
                ColorQuad(
                    a.d, a.a, b.a, b.d,
                    ColorRGBa.random(), ColorRGBa.random(), ColorRGBa.random(), ColorRGBa.random()
                )
            )
        }.flatten()

        val cube = quads.toTriangles()
        val cubeWire = quads.toWire()
        val shaded = shadeStyle {
            fragmentTransform = """
                x_fill = va_color;
                x_fill.rgb *= (v_viewNormal.z * 0.5 + 0.5);
            """.trimIndent()
        }

        //extend(ScreenRecorder())
        extend(cam)
        extend {
            drawer.clear(ColorRGBa.WHITE)

            drawer.shadeStyle = shaded
            drawer.fill = ColorRGBa.SEA_GREEN
            drawer.vertexBuffer(cube, DrawPrimitive.TRIANGLES)

//            drawer.shadeStyle = null
//            drawer.fill = ColorRGBa.BLACK
//            drawer.vertexBuffer(cubeWire, DrawPrimitive.LINES)
        }
    }
}

private fun ColorRGBa.Companion.random() = ColorRGBa(
    Random.double(0.0), Random.double(0.0), Random.double(0.0), Random.double(0.0)
)

/**
 * Converts a list of [Quad] into a vertex buffer containing pairs of
 * vertices. Each [Quad] is 4 pairs (start and end points of a segment)
 *
 * @return A [VertexBuffer] to be drawn using [DrawPrimitive.LINES]
 */
private fun List<ColorQuad>.toWire(): VertexBuffer {
    val buffer = vertexBuffer(vertexFormat {
        position(3)
    }, size * 4 * 2)
    buffer.put {
        forEach {
            write(it.a, it.b)
            write(it.b, it.c)
            write(it.c, it.d)
            write(it.d, it.a)
        }
    }
    return buffer
}

/**
 * Converts a list of [Quad] into a vertex buffer containing triplets of
 * vertices defining triangles and a normal for each vertex. That means that
 * each [Quad] becomes 2 triangles (6 vertices) and 6 normals. There is some
 * duplication as all six vertices have the same normals (the whole quad
 * has the same normal in each of the 6 vertices).
 *
 * @return A [VertexBuffer] to be drawn using [DrawPrimitive.TRIANGLES]
 */
private fun List<ColorQuad>.toTriangles(): VertexBuffer {
    val quadCount = size
    val buffer = vertexBuffer(vertexFormat {
        position(3) // = vec3
        normal(3)   // = vec3
        color(4)    // = vec3
    }, quadCount * 6 * 2) // 6 verts per quad, times 2 (because we create position AND normal)
    buffer.put {
        forEach {
            it.run {
                val up = (b - a).cross(d - a).normalized
                write(a, up)
                write(aCol.toVector4())
                write(b, up)
                write(bCol.toVector4())
                write(c, up)
                write(cCol.toVector4())
                write(a, up)
                write(aCol.toVector4())
                write(c, up)
                write(cCol.toVector4())
                write(d, up)
                write(dCol.toVector4())
            }
        }
    }
    return buffer
}
