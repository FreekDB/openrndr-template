package apps.editablecurve

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
                val curve = EditableCurveState.addCurve()
                EditableCurveState.sNumSubcurves.value = curve.numSubcurves.toDouble()
                EditableCurveState.sSeparation.value = curve.separation
            }
        }
        button {
            label = "Remove curve"
            clicked { EditableCurveState.removeCurve() }
        }
        button {
            label = "Clear"
            clicked { EditableCurveState.removeAllCurves() }
        }
        button {
            label = "Save SVG"
            clicked { EditableCurveState.saveSVG = true }
        }
        button {
            label = "Save design"
            // show system dialog, serialize data, save it
            clicked {
                // See
                // https://github.com/openrndr/orx/blob/master/orx-gui/src/main/kotlin/Gui.kt
                // and https://github.com/edwinRNDR/panel-examples/tree/master/src/main/kotlin
                saveFileDialog(supportedExtensions = listOf("json")) {
                    EditableCurveState.saveFile(it)
                }
            }
        }
        button {
            label = "Load design"
            // show system dialog, load, unserialize
            clicked {
                openFileDialog(supportedExtensions = listOf("json")) {
                    EditableCurveState.loadFile(it)
                }
            }
        }
        EditableCurveState.sNumSubcurves = slider {
            label = "Subcurves"
            value = 0.0
            range = Range(0.0, 30.0)
            precision = 0
            events.valueChanged.listen {
                EditableCurveState.setSubcurves(it.newValue)
            }
        }
        EditableCurveState.sSeparation = slider {
            label = "Separation"
            value = 1.0
            range = Range(-50.0, 50.0)
            precision = 0
            events.valueChanged.listen {
                EditableCurveState.setSeparation(it.newValue)
            }
        }

    }
}
