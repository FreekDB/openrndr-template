package p5

import org.openrndr.WindowMultisample
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.DrawPrimitive
import org.openrndr.draw.loadImage
import org.openrndr.draw.shadeStyle
import org.openrndr.extras.camera.Orbital
import org.openrndr.extras.meshgenerators.boxMesh
import org.openrndr.math.Vector3


fun main() = application {
    configure {
        width = 640
        height = 360
        multisample = WindowMultisample.SampleCount(2)
    }
    program {
        val cube = boxMesh()
        val tex = loadImage("/usr/share/processing/modes/java/examples/Topics/Textures/TextureCube/data/berlin-1.jpg")
        val cam = Orbital()
        cam.eye = -Vector3.UNIT_Z * 150.0

        extend(cam)
        extend {
            drawer.background(ColorRGBa.WHITE)
            drawer.shadeStyle = shadeStyle {
                fragmentTransform = "x_fill = texture(p_tex, va_texCoord0.xy);"
                parameter("tex", tex)
            }
            drawer.scale(90.0)
            drawer.vertexBuffer(cube, DrawPrimitive.TRIANGLES)
        }
    }
}
