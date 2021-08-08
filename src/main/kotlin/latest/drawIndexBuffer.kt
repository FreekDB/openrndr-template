package latest


import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.extras.color.presets.MAGENTA
import org.openrndr.extras.color.presets.ORANGE
import org.openrndr.math.Vector3
import java.nio.ByteBuffer
import java.nio.ByteOrder

/*
Example using index buffer
Draw 2 quads with 6 vertices and 12 indices
 0        1        2
 +--------+--------+
 |      / |       /|
 |    /   |     /  |
 |  /     |   /    |
 |/       | /      |
 +--------+--------+
 3        4        5
*/
data class Vertex(val position: Vector3, val color: ColorRGBa)

fun main() = applicationSynchronous {
    program {
        fun winPos(u: Double, v: Double) = drawer.bounds.position(u, v).xy0

        val vertices = listOf(
            Vertex(winPos(0.25, 0.25), ColorRGBa.RED), //0
            Vertex(winPos(0.50, 0.25), ColorRGBa.BLUE), //1
            Vertex(winPos(0.75, 0.25), ColorRGBa.GREEN), //2
            Vertex(winPos(0.25, 0.75), ColorRGBa.MAGENTA), //3
            Vertex(winPos(0.50, 0.75), ColorRGBa.YELLOW), //4
            Vertex(winPos(0.75, 0.75), ColorRGBa.ORANGE), //5
        )
        // Note: don't mix windings! Here all clockwise.
        val indices = listOf(
            0, 1, 3,
            1, 4, 3,
            1, 2, 4,
            2, 5, 4
        )

        // VertexBuffer
        val vb = vertexBuffer(vertexFormat {
            attribute(Vertex::position)
            attribute(Vertex::color)
            //position(3)
            //color(4)
        }, vertices.size)

        val x = Vertex::position

        vb.put {
            vertices.forEach {
                write(it.position)
                write(it.color)
            }
        }

        // IndexBuffer
        val ib = indexBuffer(indices.size, IndexType.INT16)
        val bb = ByteBuffer.allocateDirect(ib.indexCount * ib.type.sizeInBytes)
        bb.order(ByteOrder.nativeOrder()) //something to do with endianness
        indices.forEach {
            bb.putShort(it.toShort()) //kotlin equivalent of Int16 is Short
        }
        bb.rewind() //return the position of ByteBuffer back to 0 before writing to index buffer
        ib.write(bb)

        extend {
            drawer.shadeStyle = shadeStyle {
                vertexTransform = "va_color = a_color;"
                fragmentTransform = "x_fill = va_color;"
            }
            drawer.vertexBuffer(ib, listOf(vb), DrawPrimitive.TRIANGLES)
        }
    }
}

