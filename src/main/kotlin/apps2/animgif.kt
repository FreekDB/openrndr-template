package apps2

import org.openrndr.application
import org.openrndr.extra.videoprofiles.PNGProfile
import org.openrndr.ffmpeg.ScreenRecorder
import org.openrndr.ffmpeg.VideoWriterProfile
import org.openrndr.math.Vector2
import java.lang.Math.toDegrees
import kotlin.math.PI
import kotlin.math.sin

fun main() = application {
    configure {
        width = 512
        height = 512
    }
    program {
        val totalFrames = 12

        extend(ScreenRecorder()) {
            profile = PNGProfile()
            outputFile = "new-%02d.png"
        }
        extend {
            val norm = (frameCount % totalFrames) / totalFrames.toDouble() // cycles 0.0 .. 1.0
            val radians = 2 * PI * norm // cycles 0.0 .. TWO_PI
            val angle = toDegrees(radians) // cycles 0.0 .. 360.0

            // Draw stuff here.
            // You can use one of the three variables above
            // to produce looping animations.
            drawer.circle(200 + 50 * sin(radians), 200.0, 100.0)
            drawer.rectangle(Vector2.ZERO, 20.0, 20.0)
            drawer.translate(200.0, 200.0)
            drawer.rotate(angle)
            drawer.rectangle(0.0, 0.0, 10.0, 100.0)

            if (frameCount == totalFrames) {
                application.exit()
            }
        }
    }
}
