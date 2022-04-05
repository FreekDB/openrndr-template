package apps.p5

import org.openrndr.WindowMultisample
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.DrawPrimitive
import org.openrndr.draw.loadImage
import org.openrndr.draw.shadeStyle
import org.openrndr.extras.camera.Orbital
import org.openrndr.extras.meshgenerators.boxMesh
import org.openrndr.math.Vector3

/**
 * id: f9f33c54-8f89-4929-afe2-348d6c785b39
 * description: New sketch
 * tags: #new
 */

fun main() = application {
    configure {
        width = 640
        height = 360
        multisample = WindowMultisample.SampleCount(2)
    }
    program {
        val cube = boxMesh()
        val tex = loadImage("data/images/cheeta.jpg")
        val cam = Orbital()
        cam.eye = -Vector3.UNIT_Z * 150.0

        extend(cam)
        extend {
            drawer.clear(ColorRGBa.WHITE)
            drawer.shadeStyle = shadeStyle {
                fragmentTransform = """
                    x_fill = texture(p_tex, va_texCoord0.xy);
                    
                    // silly vertical light simulation
                    vec3 n = (u_viewNormalMatrix * vec4(va_normal, 1.0)).xyz;
                    x_fill.rgb += dot(n, vec3(0.0, 1.0, 0.0)) * 0.5;
                """
                parameter("tex", tex)
            }
            drawer.scale(90.0)
            drawer.vertexBuffer(cube, DrawPrimitive.TRIANGLES)
        }
    }
}
