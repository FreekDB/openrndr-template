package editablecurve

import org.openrndr.Program
import org.openrndr.color.ColorRGBa
import org.openrndr.dialogs.openFileDialog
import org.openrndr.dialogs.saveFileDialog
import org.openrndr.panel.controlManager
import org.openrndr.panel.elements.*
import org.openrndr.panel.style.*

fun Program.setupUI() = controlManager {
    styleSheet(has type "button") {
        background = Color.RGBa(ColorRGBa(0.5, 0.8, 0.9))
        color = Color.RGBa(ColorRGBa.BLACK)
    }
    styleSheet(has type "slider") {
        color = Color.RGBa(ColorRGBa(0.5, 0.8, 0.9))
        width = 200.px
    }

    layout {
        button {
            label = "Add curve"
            clicked {
                val c = ECState.addCurve()
                ECState.sNumSubcurves.value = c.numSubcurves.toDouble()
                ECState.sSeparation.value = c.separation
            }
        }
        button {
            label = "Remove curve"
            clicked { ECState.removeCurve() }
        }
        button {
            label = "Clear"
            clicked { ECState.removeAllCurves() }
        }
        button {
            label = "Save SVG"
            clicked { ECState.saveSVG = true }
        }
        button {
            label = "Save design"
            // show system dialog, serialize data, save it
            clicked {
                // See
                // https://github.com/openrndr/orx/blob/master/orx-gui/src/main/kotlin/Gui.kt
                // and https://github.com/edwinRNDR/panel-examples/tree/master/src/main/kotlin
                saveFileDialog(supportedExtensions = listOf("json")) {
                    ECState.saveFile(it.ensureExtension(".json"))
                }
            }
        }
        button {
            label = "Load design"
            // show system dialog, load, unserialize
            clicked {
                openFileDialog(supportedExtensions = listOf("json")) {
                    ECState.loadFile(it)
                }
            }
        }
        ECState.sNumSubcurves = slider {
            label = "Subcurves"
            value = 0.0
            range = Range(0.0, 30.0)
            precision = 0
            events.valueChanged.subscribe {
                ECState.setSubcurves(it.newValue)
            }
        }
        ECState.sSeparation = slider {
            label = "Separation"
            value = 1.0
            range = Range(-50.0, 50.0)
            precision = 0
            events.valueChanged.subscribe {
                ECState.setSeparation(it.newValue)
            }
        }

    }
}