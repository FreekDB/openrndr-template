package apps.p5

import aBeLibs.random.sign
import org.openrndr.WindowMultisample
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.extra.noise.Random
import org.openrndr.extra.noise.uniform
import org.openrndr.extras.camera.Orbital
import org.openrndr.extras.color.presets.DARK_SEA_GREEN
import org.openrndr.extras.color.presets.DIM_GRAY
import org.openrndr.extras.color.presets.SEA_GREEN
import org.openrndr.math.Vector3

/**
 * id: 4b11876b-63bc-4491-9e0d-b41ef1bb8b3e
 * description: Shows how to render a list of Quads as shaded triangles
 * and edges. Can be used to create designs based on 3D Quads and render them.
 * tags: #3D #Quad
 */

data class Quad(val a: Vector3, val b: Vector3, val c: Vector3, val d: Vector3)

fun main() = application {
    configure {
        width = 400
        height = 400
        multisample = WindowMultisample.SampleCount(4)
    }
    program {
        val cam = Orbital()
        cam.eye = -Vector3.UNIT_Z * 150.0

        /**
         *               5              6
         *              +--------------+
         *             /|             /|
         *            / |            / |
         *           /  |           /  |
         *          +--------------+   |
         *        1 |   |        2 |   |
         *          |   |          |   |
         *          |   |4         |   |7
         *          |   +----------|---+
         *          |  /           |  /
         *          | /            | /
         *          |/             |/
         *          +--------------+
         *        0              3
         */

        // The vertices of a cube
        val cubeVerts = listOf(
            Vector3(-50.0, -50.0, -50.0),
            Vector3(-50.0, 50.0, -50.0),
            Vector3(50.0, 50.0, -50.0),
            Vector3(50.0, -50.0, -50.0), // front
            Vector3(-50.0, -50.0, 50.0),
            Vector3(-50.0, 50.0, 50.0),
            Vector3(50.0, 50.0, 50.0),
            Vector3(50.0, -50.0, 50.0) // back
        )

        // Here we create the 3D design, in this case a simple Cube.
        // We could do any other shape based on Quads. A Quad cloud for instance. Or something like:
        // https://openrndr.discourse.group/t/is-there-a-way-to-batch-draw-these-rectangles/356

//        val quads = mutableListOf<Quad>()
//        quads.add(Quad(cubeVerts[0], cubeVerts[1], cubeVerts[2], cubeVerts[3])) // front
//        quads.add(Quad(cubeVerts[7], cubeVerts[6], cubeVerts[5], cubeVerts[4])) // back
//        quads.add(Quad(cubeVerts[4], cubeVerts[5], cubeVerts[1], cubeVerts[0])) // left
//        quads.add(Quad(cubeVerts[3], cubeVerts[2], cubeVerts[6], cubeVerts[7])) // right
//        quads.add(Quad(cubeVerts[1], cubeVerts[5], cubeVerts[6], cubeVerts[2])) // top
//        quads.add(Quad(cubeVerts[3], cubeVerts[7], cubeVerts[4], cubeVerts[0])) // bottom

        val quads = List(50) {
            val orientation = listOf(
                Vector3.UNIT_Z, Vector3.UNIT_X, Vector3.UNIT_Y
            ).shuffled().take(2).map {
                it * Random.sign()
            }
            val a = Vector3.uniform(-50.0, 50.0)
            val dx = orientation[0] * 20.0
            val dy = orientation[1] * 20.0
            Quad(a, a + dx, a + dx + dy, a + dy)
        }

        val cube = quads.toTriangles()
        val cubeWire = quads.toWire()
        val shaded = shadeStyle {
            fragmentTransform = "x_fill.rgb *= v_viewNormal.z;"
        }

        //extend(ScreenRecorder()) { profile = GIFProfile() }
        extend(cam)
        extend {
            drawer.clear(ColorRGBa.DIM_GRAY)

            drawer.shadeStyle = shaded
            drawer.fill = ColorRGBa.SEA_GREEN
            drawer.vertexBuffer(cube, DrawPrimitive.TRIANGLES)

            drawer.shadeStyle = null
            drawer.fill = ColorRGBa.DARK_SEA_GREEN
            drawer.vertexBuffer(cubeWire, DrawPrimitive.LINES)
        }
    }
}

/**
 * Converts a list of [Quad] into a vertex buffer containing pairs of
 * vertices. Each [Quad] is 4 pairs (start and end points of a segment)
 *
 * @return A [VertexBuffer] to be drawn using [DrawPrimitive.LINES]
 */
private fun List<Quad>.toWire(): VertexBuffer {
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
private fun List<Quad>.toTriangles(): VertexBuffer {
    val quadCount = size
    val buffer = vertexBuffer(vertexFormat {
        position(3) // = vec3
        normal(3)   // = vec3
    }, quadCount * 6 * 2) // 6 verts per quad, times 2 (because we create position AND normal)
    buffer.put {
        forEach {
            val up = (it.b - it.a).cross(it.d - it.a).normalized
            write(it.a, up)
            write(it.b, up)
            write(it.c, up)
            write(it.a, up)
            write(it.c, up)
            write(it.d, up)
        }
    }
    return buffer
}
