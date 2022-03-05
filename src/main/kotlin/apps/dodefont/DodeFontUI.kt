package apps.dodefont

import org.openrndr.Program
import org.openrndr.color.ColorRGBa
import org.openrndr.panel.controlManager
import org.openrndr.panel.elements.*
import org.openrndr.panel.style.*

/**
 * id: 0e81717b-e993-403d-92b5-fe122b59b448
 * description: New sketch
 * tags: #new
 */

fun Program.setupUI() = controlManager {
    controlManager.fontManager.register("small", "file:/home/funpro/OR/openrndr-template/data/fonts/slkscr.ttf")

    styleSheet {
        fontSize = 14.8.px
    }

    styleSheet(has type "button") {
        background = Color.RGBa(ColorRGBa.PINK)
        color = Color.RGBa(ColorRGBa.BLACK)
        fontSize = 14.8.px
        fontFamily = "small"
    }
    styleSheet(has type "slider") {
        width = 200.px
    }
    layout {
        button {
            label = "Add curve"
            clicked { TPState.rnd() }
        }
        button {
            label = "Remove curve"
            clicked { TPState.rnd() }
        }
        button {
            label = "Clear"
            clicked { TPState.rnd() }
        }
        dropdownButton(label = "I/O") {
            item {
                label = "Save SVG"
                events.picked.listen {
                    println("Save SVG")
                }
            }
            item {
                label = "Save design"
                events.picked.listen {
                    println("Save design")
                }
            }
            item {
                label = "Load design"
                events.picked.listen {
                    println("Load design")
                }
            }
        }

        toggle {
            label = "camera interaction"
            events.valueChanged.listen {
                TPState.controls.enabled = it.newValue
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
            events.valueChanged.listen {
                println("subcurves ${it.newValue}")
            }
        }
        slider {
            label = "Separation"
            value = 1.0
            range = Range(-50.0, 50.0)
            precision = 0
            events.valueChanged.listen {
                println("separation ${it.newValue}")
            }
        }
    }
}