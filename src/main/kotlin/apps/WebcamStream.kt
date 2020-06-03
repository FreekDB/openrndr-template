package apps

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.ColorFormat
import org.openrndr.draw.ColorType
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.renderTarget
import org.openrndr.extra.noise.Random
import org.openrndr.ffmpeg.PlayMode
import org.openrndr.ffmpeg.VideoPlayerFFMPEG
import org.openrndr.math.Vector2
import java.io.FileOutputStream
import java.nio.ByteBuffer
import kotlin.math.sin

/**
 * Streaming webcam to jitsi
 */

const val virtualCamDevice = "/dev/video2"
const val realCamDevice = "/dev/video4"

fun main() = application {
    configure {
        width = 640
        height = 480
    }

    program {
        println(VideoPlayerFFMPEG.listDeviceNames())

        val rt = renderTarget(width, height) {
            colorBuffer(ColorFormat.BGR, ColorType.UINT8)
        }

        // Virtual camera data structures
        val sz = width * height * 3
        val virtualCam = FileOutputStream(virtualCamDevice)
        val pixels = rt.colorBuffer(0)
        val buffer = ByteBuffer.allocateDirect(sz)
        val bytes = ByteArray(sz)

        // Real webcam
        val webcam = VideoPlayerFFMPEG.fromDevice(realCamDevice, PlayMode.VIDEO)
        webcam.play()

        // For the animated circles
        val positions = List(10) {
            Vector2(
                Random.double0(width * 1.0),
                Random.double0(height * 1.0)
            )
        }
        val radii = MutableList(positions.size) { 0.0 }

        extend {
            // Update circle radii
            for (i in 0 until radii.size) {
                radii[i] = 50.0 + 30 * sin(seconds * 5.0 + i)
            }

            // Draw webcam and circles on a layer
            drawer.isolatedWithTarget(rt) {
                webcam.draw(this)
                fill = ColorRGBa.PINK
                stroke = null
                circles(positions, radii)
            }

            // Show that layer
            drawer.image(rt.colorBuffer(0));

            // Stream that layer out
            pixels.read(buffer)
            for (i in 0 until sz) {
                bytes[i] = buffer[sz - i - 1]
            }
            virtualCam.write(bytes);
        }
    }
}
