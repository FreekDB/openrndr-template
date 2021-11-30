package live

import org.openrndr.application
import org.openrndr.draw.renderTarget
import org.openrndr.draw.shadeStyle
import org.openrndr.extra.fx.blur.BoxBlur
import org.openrndr.ffmpeg.VideoPlayerFFMPEG
import org.openrndr.shape.Circle
import kotlin.math.cos
import kotlin.math.sin

fun main() = application {
    configure {
        width = 1920
        height = 1080
    }
    program {
        val videoPlayer = VideoPlayerFFMPEG.fromDevice()
        videoPlayer.play()

        val blur = BoxBlur()
        val rt = renderTarget(width, height) {
            colorBuffer()
        }
        val shape = Circle(width / 2.0, height / 2.0, 400.0).shape

        extend {
            //drawer.clear(ColorRGBa.BLACK)
            drawer.withTarget(rt) {
                // TODO: change the following dimensions with .cover
                videoPlayer.draw(drawer, 0.0, 0.0, width.toDouble(), height.toDouble())
            }
            blur.apply(rt.colorBuffer(0), rt.colorBuffer(0))

            drawer.shadeStyle = shadeStyle {
                fragmentTransform = """
                        vec2 texCoord = c_boundsPosition.xy;
                        texCoord.y = 1.0 - texCoord.y;
                        vec2 size = textureSize(p_image, 0);
                        texCoord.x /= size.x/size.y;
                        x_fill = texture(p_image, texCoord) * vec4(1.0, 0.9, 0.4, 1.0);
                    """
                parameter("image", rt.colorBuffer(0))
            }

            drawer.translate(cos(seconds) * 100.0, sin(seconds) * 100.0)
            drawer.shape(shape)

            //drawer.image(rt.colorBuffer(0))
        }
    }
}
