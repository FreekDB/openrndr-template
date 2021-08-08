package apps

import org.openrndr.KEY_ESCAPE
import org.openrndr.applicationSynchronous
import org.openrndr.color.rgb
import org.openrndr.draw.DrawPrimitive
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.renderTarget
import org.openrndr.draw.shadeStyle
import org.openrndr.extra.keyframer.Keyframer
import org.openrndr.extra.noise.Random
import org.openrndr.extras.camera.OrbitalCamera
import org.openrndr.extras.camera.isolated
import org.openrndr.extras.meshgenerators.dodecahedronMesh
import org.openrndr.ffmpeg.VideoWriter
import org.openrndr.math.Vector3
import org.openrndr.math.map
import java.io.File
import kotlin.system.exitProcess

/**
 * Test the Keyframer
 * Produces output.mp4 and quits after 150 frames
 */

class Animation : Keyframer() {
    val scale by DoubleChannel("scale")
    val jitter by DoubleChannel("jitter")
    val position by Vector2Channel(arrayOf("x", "y"))
    val rot by Vector2Channel(arrayOf("rotx", "roty"))
    //val color by RGBChannel(arrayOf("r", "g", "b"))
}

fun main() = applicationSynchronous {
    configure {
        width = 800
        height = 800
    }

    program {
        val rt = renderTarget(width, height, 1.0) {
            colorBuffer()
            depthBuffer()
        }

        val videoWriter = VideoWriter.create().size(width, height).output("output.mp4").start()

        val dode = dodecahedronMesh(60.0)
        val camera = OrbitalCamera(Vector3.UNIT_Z * 1500.0, Vector3.ZERO, 60.0, 0.1, 2000.0)

        val animation = Animation()
        animation.loadFromJson(File("data/animation.json"))

        //extend(ScreenRecorder())
        extend {
            drawer.isolatedWithTarget(rt) {
                drawer.clear(rgb(0.85, 0.42, 0.29))

                camera.update(deltaTime)
                for (i in 0..15) {
                    camera.isolated(drawer) {
                        drawer.shadeStyle = shadeStyle {
                            fragmentTransform =
                                """
                                    float m = dot(v_worldNormal, normalize(vec3(1.0, 0.5, 0.2))) * 0.5 + 0.5;
                                    x_fill.rgb *= mix(vec3(0.39, 0.73, 0.85), vec3(0.90, 0.90, 0.75), m * m * m);
                                """.trimIndent()
                        }

                        val t = (frameCount % 150) / 150.0
                        animation((t * 5 + 2 + Random.simplex(i * 1.2, 0.1)) % 5.0)
                        drawer.translate(
                            map(0.0, 3.0, -500.0, 500.0, (i % 4).toDouble()),
                            map(0.0, 3.0, -500.0, 500.0, (i / 4).toDouble())
                        )
                        drawer.translate(
                            animation.position.vector3(z = 0.0) +
                                    Random.vector3(-5.0, 5.0) * animation.jitter
                        )
                        drawer.rotate(Vector3.UNIT_Y, animation.rot.y)
                        drawer.rotate(Vector3.UNIT_X, animation.rot.x)
                        drawer.scale(animation.scale)
                        drawer.vertexBuffer(dode, DrawPrimitive.TRIANGLES)
                    }
                }
            }

            videoWriter.frame(rt.colorBuffer(0))
            drawer.image(rt.colorBuffer(0))

            if (frameCount >= 150) {
                videoWriter.stop()
                exitProcess(0)
            }
        }

        keyboard.keyDown.listen {
            when (it.key) {
                KEY_ESCAPE -> exitProcess(0)
            }
        }

    }
}
