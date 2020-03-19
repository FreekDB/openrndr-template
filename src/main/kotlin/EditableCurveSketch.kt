import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.dialogs.openFileDialog
import org.openrndr.dialogs.saveFileDialog
import org.openrndr.ffmpeg.ScreenRecorder
import org.openrndr.math.IntVector2
import org.openrndr.math.Vector2
import org.openrndr.math.clamp
import org.openrndr.panel.ControlManager
import org.openrndr.panel.elements.*
import org.openrndr.panel.layout
import org.openrndr.panel.style.*
import org.openrndr.panel.style.Color.RGBa
import org.openrndr.panel.styleSheet
import org.openrndr.shape.CompositionDrawer
import org.openrndr.shape.ShapeContour
import org.openrndr.svg.writeSVG
import java.io.File

fun main() = application {
    configure {
        width = 900
        height = 900
        position = IntVector2(10, 10)
    }

    program {
        extend(ScreenRecorder())

        var bgColor = ColorRGBa.WHITE.shade(0.95)

        var activeCurve = 0
        var curves = mutableListOf<EditableCurve>()
        var segments = mutableListOf<ShapeContour>()

        var paramNumSubcurves = Slider()
        var paramSep = Slider()

        var saveSVG = false
        var curvesNeedUpdate = false

        var mouseClickStart = Vector2(0.0)

        EditableCurve.colorEdit = ColorRGBa(0.5, 0.8, 0.9)

        fun selectCurve(pos: Vector2): Int {
            // try select a curve
            var maxd = Double.MAX_VALUE
            var closestCurveId = curves.size - 1
            for (i in 0 until curves.size) {
                var d = curves[i].distanceTo(pos)
                if (d < maxd) {
                    maxd = d
                    closestCurveId = i
                }
            }
            return closestCurveId
        }

        fun refreshCurves() {
            segments.clear()
            curves.forEach { it.addSegmentsTo(segments) }
        }

        fun addCurve() {
            var c = EditableCurve()
            curves.add(c)

            activeCurve = curves.size - 1;

            c.randomize()
            c.addSegmentsTo(segments)
            paramNumSubcurves.value = c.getNumSubcurves().toDouble()
            paramSep.value = c.getSep().toDouble()
        }

        extend(ControlManager()) {
            styleSheet(has type "button") {
                background = RGBa(ColorRGBa(0.5, 0.8, 0.9))
                color = RGBa(ColorRGBa.BLACK)
            }
            styleSheet(has type "slider") {
                color = RGBa(ColorRGBa(0.5, 0.8, 0.9))
                width = 200.px
            }
            layout {
                button {
                    label = "Add curve"
                    clicked { addCurve() }
                }
                button {
                    label = "Remove curve"
                    clicked {
                        curves.removeAt(activeCurve)
                        activeCurve = activeCurve.coerceIn(0, curves.size - 1)
                    }
                }
                button {
                    label = "Clear"
                    clicked {
                        segments.clear()
                        curves.clear()
                        activeCurve = curves.size - 1
                    }
                }
                button {
                    label = "Save SVG"
                    clicked { saveSVG = true }
                }
                button {
                    label = "Save design"
                    // show system dialog, serialize data, save it
                    clicked {
                        // See
                        // https://github.com/openrndr/orx/blob/master/orx-gui/src/main/kotlin/Gui.kt
                        // and https://github.com/edwinRNDR/panel-examples/tree/master/src/main/kotlin
                        saveFileDialog(supportedExtensions = listOf("json")) {
                            val gson = Gson()
                            val json = gson.toJson(curves)

                            it.writeText(json)
                        }
                    }
                }
                button {
                    label = "Load design"
                    // show system dialog, load, unserialize
                    clicked {
                        openFileDialog(supportedExtensions = listOf("json")) {
                            var gson = Gson()
                            val typeToken = object : TypeToken<MutableList<EditableCurve>>() {}
                            curves = gson.fromJson(it.readText(), typeToken.type)
                            curves.forEach { it.update() }
                            refreshCurves()
                        }
                    }
                }
                paramNumSubcurves = slider {
                    label = "Subcurves"
                    value = 0.0
                    range = Range(0.0, 30.0)
                    precision = 0
                    events.valueChanged.subscribe {
                        curves[activeCurve].setNumSubcurves(it.newValue.toInt())
                        refreshCurves()
                    }
                }
                paramSep = slider {
                    label = "Separation"
                    value = 1.0
                    range = Range(-50.0, 50.0)
                    precision = 0
                    events.valueChanged.subscribe {
                        curves[activeCurve].setSep(it.newValue.toInt())
                        refreshCurves()
                    }
                }
            }
        }

        extend {
            drawer.background(bgColor)

            if (saveSVG) {
                val svg = CompositionDrawer()

                svg.fill = null
                svg.stroke = ColorRGBa.BLACK

                segments.forEach { svg.contour(it) }

                File("out/output.svg").writeText(writeSVG(svg.composition))

                saveSVG = false;
            }

            //ofSetColor(jsonToColor(cfg["lineColor"]));

            segments.forEach { drawer.contour(it) }

            curves.forEachIndexed { i, curve -> curve.draw(drawer, i == activeCurve) }
        }

        keyboard.keyDown.listen {
            if (it.key == KEY_ESCAPE) {
                application.exit()
            }
        }

        mouse.buttonDown.listen {
            if (activeCurve < curves.size) {
                curves[activeCurve].mousePressed(it.position)
            }
            mouseClickStart = it.position;

        }

        mouse.dragged.listen {
            if (activeCurve < curves.size) {
                curves[activeCurve].mouseDragged(
                    Vector2(
                        clamp(it.position.x, 0.0, width.toDouble()),
                        clamp(it.position.y, 0.0, height.toDouble())
                    )
                )
                // TODO: can be optimized by observing if a curve was modified
                curvesNeedUpdate = true
            }

        }

        mouse.buttonUp.listen { mouse ->
            if (curves.size == 0) {
                return@listen
            }
            val dist = (mouse.position - mouseClickStart).length
            if (dist < 10) {
                activeCurve = selectCurve(mouse.position)
                var curve = curves[activeCurve]
                paramNumSubcurves.value = curve.getNumSubcurves().toDouble()
                paramSep.value = curve.getSep().toDouble()
            }
            if (curvesNeedUpdate) {
                refreshCurves()
                curvesNeedUpdate = false
            }

        }
    }
}