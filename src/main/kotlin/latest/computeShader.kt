package latest


import org.openrndr.application
import org.openrndr.draw.*
import org.openrndr.math.Vector2
import java.io.File

fun main() {
    application {
        program {
            val cs = ComputeShader.fromCode(File("data/shaders/cs1.glsl")
                .readText(), "cc1")

            val tempBuffer = loadImage("data/images/1023px-The_Earth_seen_from_Apollo_17.jpg")
            val inputBuffer = colorBuffer(width, height)
            tempBuffer.copyTo(inputBuffer)
            val outputBuffer = colorBuffer(width, height)

            cs.uniform("resolution", Vector2(width * 1.0, height * 1.0))

            extend {
                cs.uniform("seconds", seconds)
                cs.image("inputImg", 0, inputBuffer.imageBinding(0, ImageAccess.READ))
                cs.image("outputImg",1, outputBuffer.imageBinding(0, ImageAccess.WRITE))
                cs.execute(outputBuffer.width, outputBuffer.height, 1)
                drawer.image(outputBuffer)
            }
        }
    }
}
