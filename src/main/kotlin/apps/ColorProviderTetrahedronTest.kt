package apps

import aBeLibs.color.ColorProviderTetrahedron
import aBeLibs.math.doubleExponentialSigmoid
import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.Random
import org.openrndr.extra.shadestyles.linearGradient
import org.openrndr.math.Vector3
import org.openrndr.panel.ControlManager
import org.openrndr.panel.elements.button
import org.openrndr.panel.elements.div
import org.openrndr.panel.elements.requestRedraw
import org.openrndr.panel.layout
import org.openrndr.panel.style.*
import org.openrndr.panel.styleSheet
import kotlin.system.exitProcess

/**
 * Testing Lab and RGB color mixing.
 * Based on these 3 palettes, RGB mixing is more pleasant
 * Lab seems to produce darker results
 */

fun main() = application {
    configure {
        width = 1500
        height = 800
    }

    program {
        var modeRGB = true;
        val palettes = listOf(
            ColorProviderTetrahedron("E1AA66", "8E4770", "7F615B", "8CA9B6", "A"),
            ColorProviderTetrahedron("302739", "3C8C9E", "82BBB6", "E1C367", "B"),
            ColorProviderTetrahedron("279193", "B1F8C9", "F5C579", "EC3F4F", "C"),
            ColorProviderTetrahedron("5B4C4D", "A7A994", "CFC9A7", "D04F27", "D")
        )
        var palette = palettes[0]

        extend(ControlManager()) {
            styleSheet(has class_ "horizontal") {
                display = Display.FLEX
                flexDirection = FlexDirection.Row
            }
            layout {
                div("horizontal") {
                    button(label = "RGB") {
                        events.clicked.listen {
                            modeRGB = !modeRGB
                            this.label = if (modeRGB) "RGB" else "Lab"
                            requestRedraw()
                        }
                    }
                    button(label = "Palette") {
                        events.clicked.listen {
                            Random.seed = System.currentTimeMillis().toString()
                            palette = Random.pick(palettes)
                            this.label = "Palette ${palette.name}"
                            requestRedraw()
                        }
                    }
                }
            }
        }
        extend(Screenshots())
        extend {
            Random.resetState()
            for (x in 0 until width step 50) {
                for (y in 0 until width step 50) {
                    //val p = Vector3(Random.double0(1.0), Random.double0(1.0), Random.double0(1.0))
                    val p = Vector3(
                        doubleExponentialSigmoid(x * 0.001, 0.0),
                        doubleExponentialSigmoid(0.0, y * 0.001),
                        doubleExponentialSigmoid(x * 0.001, y * 0.001)
                    )
                    drawer.stroke = null //ColorRGBa.WHITE.opacify(0.7)
                    drawer.fill = if (modeRGB) palette.getColorViaRGB(p) else palette.getColor(p)
                    drawer.shadeStyle = linearGradient(
                        ColorRGBa.WHITE,
                        ColorRGBa.WHITE.shade(0.7),
                        rotation = Random.value(x * 0.001, y * 0.001) * 180 + 180,
                        exponent = 4.0
                    )
                    drawer.rectangle(x * 1.0, y * 1.0, 50.0, 50.0)
                }
            }
        }

        keyboard.keyDown.listen {
            when (it.key) {
                KEY_ESCAPE -> exitProcess(0)
            }
        }
    }
}

//private operator fun Vector3.plus(d: Double): Vector3 {
//    return Vector3(this.x + d, this.y + d, this.z + d)
//}
