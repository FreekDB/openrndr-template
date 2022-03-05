package apps5

import org.openrndr.WindowMultisample
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.DrawPrimitive
import org.openrndr.draw.shadeStyle
import org.openrndr.extensions.Screenshots
import org.openrndr.extras.meshgenerators.toMesh
import org.openrndr.math.Vector2

/**
 * id: 04141314-4f70-4f6f-9891-4d4a718d1f60
 * description: New sketch
 * tags: #new
 */

fun main() = application {
    configure {
        width = 800
        height = 800
        multisample = WindowMultisample.SampleCount(8)
    }

    program {
        val rect = drawer.bounds.offsetEdges(-150.0)
        val mesh = rect.toMesh(2.0)

        val gradient = shadeStyle {
            fragmentTransform = """
                    vec2 coord = (va_texCoord0.xy - 0.5 + p_offset);

                    float angle = radians(p_rotation);
                    float cr = cos(angle);
                    float sr = sin(angle);
                    mat2 rm = mat2(cr, -sr, sr, cr);
                    vec2 rc = rm * coord;
                    float f = clamp(rc.y + 0.5, 0.0, 1.0);            

                    vec4 gradient = mix(p_color0, p_color1, pow(f, p_exponent));
                    x_fill *= gradient;
                """

            vertexTransform = """
                    x_position.x += 10.0 * sin(x_position.y * 0.03 + p_time);
                    x_position.y += 10.0 * sin(x_position.x * 0.02 + p_time);
                """.trimIndent()
        }

        extend(Screenshots())
        extend {
            drawer.clear(ColorRGBa.WHITE)
            drawer.fill = ColorRGBa.WHITE
            drawer.stroke = null

            drawer.shadeStyle = gradient

            gradient.parameter("color0", ColorRGBa.PINK)
            gradient.parameter("color1", ColorRGBa.PINK.shade(0.5))
            gradient.parameter("exponent", 4.0)
            gradient.parameter("rotation", 90.0)
            gradient.parameter("offset", Vector2.ZERO)
            gradient.parameter("time", seconds)

            drawer.vertexBuffer(mesh, DrawPrimitive.TRIANGLES)
        }
    }
}
