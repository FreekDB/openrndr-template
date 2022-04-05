package apps.dodefont

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.extras.camera.isolated
import org.openrndr.extras.meshgenerators.dodecahedronMesh
import org.openrndr.math.IntVector2
import org.openrndr.math.Vector2
import org.openrndr.shape.contour
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

/**
 * id: 5d9bcf03-845d-45e9-806b-e8033efa2876
 * description: Test dodecahedron, camera.
 * Show font bitmap which looks unsharp in my system.
 * Combine ortho with perspective.
 * tags: #new
 */

fun main() = application {
    configure {
        width = 768
        height = 576
        hideWindowDecorations = true
        position = IntVector2(10, 10)
    }

    program {
        val image = loadImage("data/images/pm5544.png")
        val font = loadFont("data/fonts/slkscr.ttf", 20.0)

        var curves = emptyList<apps.editablecurve.EditableCurve>()

        val curve = contour {
            moveTo(Vector2(0.1 * width, 0.1 * height))
            curveTo(
                Vector2(0.9 * width, 0.1 * height),
                Vector2(0.1 * width, 0.9 * height),
                Vector2(0.9 * width, 0.9 * height)
            )
        }


        val dode = dodecahedronMesh(350.0)

        //extend(camera)
        extend(TPState.controls) // adds both mouse and keyboard bindings

        extend(setupUI())

        extend {
            drawer.clear(TPState.bgColor)

            // Background image
            drawer.isolated {
                drawer.translate(20.0, 0.0)
                drawer.drawStyle.colorMatrix = tint(ColorRGBa.WHITE.shade(0.2))
                drawer.image(image)
            }

            // Curve
            drawer.isolated {
                drawer.translate(0.0, 0.0, 3.0)
                drawer.stroke = ColorRGBa.GREEN
                drawer.strokeWeight = 5.0
                drawer.contour(curve)
            }

            // Circle
            drawer.fill = ColorRGBa.PINK
            drawer.pushTransforms()
            drawer.translate(
                cos(seconds) * width / 2.0 + width / 2.0,
                sin(0.5 * seconds) * height / 2.0 + height / 2.0, 2.0
            )
            drawer.scale(1.0, 0.5)
            drawer.circle(0.0, 0.0, 140.0)
            drawer.popTransforms()

            // Draw font texture
            drawer.isolated {
                drawer.shadeStyle = shadeStyle {
                    fragmentTransform = """x_fill.gb = vec2(x_fill.r);"""
                }
                drawer.image(font.texture)
            }

            // Dode
            TPState.camera.update(deltaTime)
            TPState.camera.isolated(drawer) {
                drawer.shadeStyle = shadeStyle {
                    fragmentTransform =
                        """x_fill.rgb *= 0.8 + 0.2 * dot(v_worldNormal, normalize(vec3(1.0, 0.5, 0.2)));"""
                }
                drawer.vertexBuffer(dode, DrawPrimitive.TRIANGLES)
            }

            // Text
            drawer.translate(0.0, 0.0, 5.0)
            drawer.fontMap = font
            drawer.fill = ColorRGBa.WHITE
            drawer.text(
                "OPENRNDR",
                floor(width / 2.0) + 0.5,
                floor(height / 2.0) + 0.5
            )
        }
    }
}
