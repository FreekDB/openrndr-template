package apps

import org.openrndr.applicationSynchronous
import org.openrndr.draw.loadImage
import org.openrndr.draw.shadeStyle
import org.openrndr.extensions.Screenshots

/**
 * Example porting a shadertoy program to OPENRNDR
 */

fun main() = applicationSynchronous {
    configure {
        width = 1024
        height = 1024
    }

    program {
        // https://upload.wikimedia.org/wikipedia/commons/thumb/a/a9/Kissing_Prairie_dog_edit_3.jpg/1024px-Kissing_Prairie_dog_edit_3.jpg
        val img = loadImage("data/images/1024px-Kissing_Prairie_dog_edit_3.jpg")

        extend(Screenshots())
        extend {
            drawer.stroke = null
            drawer.shadeStyle = shadeStyle {
                fragmentPreamble = """
                    vec2 shuffle(vec2 xy, vec2 p) {
                        vec2 i = floor(fract(xy) * p) / p;
                        vec2 f = fract(xy * p) / p;   
                        i.x += step(1.-i.x, .5) / (p.x * 2.);    
                        i.y += step(1.-i.y, .5) / (p.y * 2.);    
                        return fract(i * 2. + f);
                    }
                """.trimIndent()
                fragmentTransform = """
                    vec2 uv = c_boundsPosition.xy;
                    //uv *= 0.5; // scale
                    uv.y = 1.-uv.y; // vert flip
                    uv = shuffle(uv, vec2(30., 1.));
                    uv = shuffle(uv, vec2(1., 30.));
                    x_fill = texture(p_img, uv);
                """.trimIndent()
                parameter("img", img)
            }
            drawer.rectangle(drawer.bounds)
        }
    }
}
