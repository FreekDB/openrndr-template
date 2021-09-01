package apps.simpleTests

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.extra.integralimage.FastIntegralImage

fun main() = application {
    configure {
        width = 1024
        height = 1024
    }
    program {
        val fii = FastIntegralImage()
        val target = colorBuffer(1024, 512, 1.0, ColorFormat.RGBa, ColorType.FLOAT32)
        val rt = renderTarget(1024, 512) {
            colorBuffer()
        }
        extend {
            drawer.clear(ColorRGBa.PINK)
            drawer.isolatedWithTarget(rt) {
                drawer.ortho(rt)
                drawer.clear(ColorRGBa.BLACK)
                drawer.fill = ColorRGBa.PINK.shade(1.0)
                drawer.circle(mouse.position, 128.0)
            }
            drawer.image(rt.colorBuffer(0), 0.0, 512.0)

            fii.apply(rt.colorBuffer(0), target)

            // -- here we sample from the integral image
            drawer.shadeStyle = shadeStyle {
                fragmentTransform = """
                    float w = 128.0;
                    vec2 step = 1.0 / textureSize(image, 0);
                    vec4 t11 = texture(image, va_texCoord0 + step * vec2(w,w));
                    vec4 t01 = texture(image, va_texCoord0 + step * vec2(-w,w));
                    vec4 t00 = texture(image, va_texCoord0 + step * vec2(-w,-w));
                    vec4 t10 = texture(image, va_texCoord0 + step * vec2(w,-w));
                    x_fill = (t11 - t01 - t10 + t00) / (w*w);
                """.trimIndent()
            }
            drawer.image(target)
        }
    }
}
