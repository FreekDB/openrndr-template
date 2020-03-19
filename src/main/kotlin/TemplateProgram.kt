import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.extras.camera.OrbitalCamera
import org.openrndr.extras.camera.OrbitalControls
import org.openrndr.extras.camera.isolated
import org.openrndr.extras.meshgenerators.dodecahedronMesh
import org.openrndr.math.IntVector2
import org.openrndr.math.Vector2
import org.openrndr.math.Vector3
import org.openrndr.panel.ControlManager
import org.openrndr.panel.controlManager
import org.openrndr.panel.elements.*
import org.openrndr.panel.layout
import org.openrndr.panel.style.*
import org.openrndr.panel.style.Color.RGBa
import org.openrndr.panel.styleSheet
import org.openrndr.shape.contour
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

fun main() = application {
    configure {
        width = 768
        height = 576
        hideWindowDecorations = true
        position = IntVector2(10, 10)
    }

    program {
        val image = loadImage("data/images/pm5544.png")
        val font = loadFont("file:/home/funpro/src/OR/openrndr-template/data/fonts/slkscr.ttf",
            14.0)

        var bgColor = ColorRGBa.GRAY.shade(0.250)

        var curves = emptyList<EditableCurve>()

        val curve = contour {
            moveTo(Vector2(0.1 * width, 0.1 * height))
            curveTo(
                Vector2(0.9 * width, 0.1 * height),
                Vector2(0.1 * width, 0.9 * height),
                Vector2(0.9 * width, 0.9 * height)
            )
        }

        val camera = OrbitalCamera(Vector3.UNIT_Z * 1000.0, Vector3.ZERO, 90.0, 0.1, 2000.0)
        val controls = OrbitalControls(camera, keySpeed = 10.0)

        val dode = dodecahedronMesh(350.0)

        //extend(camera)
        extend(controls) // adds both mouse and keyboard bindings

        fun rnd() {
            bgColor = ColorRGBa(Math.random(), Math.random(), Math.random())
        }

        extend(ControlManager()) {
            styleSheet {
                fontSize = 14.8.px
                fontManager.register("small", "file:/home/funpro/src/OR/openrndr-template/data/fonts/slkscr.ttf")
            }

            styleSheet(has type "button") {
                background = RGBa(ColorRGBa.PINK)
                color = RGBa(ColorRGBa.BLACK)
                fontSize = 14.8.px
                fontFamily = "small"
            }
            styleSheet(has type "slider") {
                width = 200.px
            }
            layout {
                button {
                    label = "Add curve"
                    clicked { rnd() }
                }
                button {
                    label = "Remove curve"
                    clicked { rnd() }
                }
                button {
                    label = "Clear"
                    clicked { rnd() }
                }

                dropdownButton(label = "I/O") {
                    item {
                        label = "Save SVG"
                        events.picked.subscribe {
                            println("Save SVG")
                        }
                    }
                    item {
                        label = "Save design"
                        events.picked.subscribe {
                            println("Save design")
                        }
                    }
                    item {
                        label = "Load design"
                        events.picked.subscribe {
                            println("Load design")
                        }
                    }
                }

                toggle {
                    label = "camera interaction"
                    events.valueChanged.subscribe {
                        controls.enabled = it.newValue
                    }
                }



                textfield {
                    label = "name"
                }

                slider {
                    label = "Subcurves"
                    value = 0.0
                    range = Range(0.0, 30.0)
                    precision = 0
                    events.valueChanged.subscribe {
                        println("subcurves ${it.newValue}")
                    }
                }
                slider {
                    label = "Separation"
                    value = 1.0
                    range = Range(-50.0, 50.0)
                    precision = 0
                    events.valueChanged.subscribe {
                        println("separation ${it.newValue}")
                    }
                }
            }
        }

        extend {
            //drawer.background(bgColor)

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

            drawer.isolated {
                drawer.shadeStyle = shadeStyle {
                    fragmentTransform = """x_fill.gb = vec2(x_fill.r);"""
                }
                drawer.image(font.texture)
            }

            // Dode
            camera.update(deltaTime)
            camera.isolated(drawer) {
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

        keyboard.keyDown.listen {
            if (it.key == KEY_ESCAPE) {
                application.exit()
            }
        }


    }
}