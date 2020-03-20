package editablecurve

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.dialogs.openFileDialog
import org.openrndr.dialogs.saveFileDialog
import org.openrndr.extra.midi.MidiTransceiver
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
import kotlin.system.exitProcess

fun main() = application {
    configure {
        width = 900
        height = 900
        position = IntVector2(10, 10)
    }

    program {
        extend(ScreenRecorder())

        var bgColor = ColorRGBa.WHITE.shade(0.95)

        var activeCurve: EditableCurve? = null
        var curves = mutableListOf<EditableCurve>()
        var segments = mutableListOf<ShapeContour>()

        var paramNumSubcurves = Slider()
        var paramSep = Slider()

        var saveSVG = false
        var curvesNeedUpdate = false

        var mouseClickStart = Vector2(0.0)

        EditableCurve.colorEdit = ColorRGBa(0.5, 0.8, 0.9)

//        MidiDeviceDescription.list().forEach {
//            println("${it.name}, ${it.vendor} r:${it.receive} t:${it.transmit}")
//        }

        fun refreshCurves() {
            segments.clear()
            curves.forEach { it.addSegmentsTo(segments) }
        }

        val mf = MidiTransceiver.fromDeviceVendor("Twister [hw:2,0,0]", "ALSA (http://www.alsa-project.org)")
        mf.controlChanged.listen {
            when (it.control) {
                12 -> {
                    activeCurve?.setNumSubcurves(it.value / 4)
                    refreshCurves()
                }
                13 -> {
                    activeCurve?.setSep(it.value - 64)
                    refreshCurves()
                }
                else -> println("${it.channel} ${it.control} ${it.value}")
            }
        }

        fun selectCurve(pos: Vector2): EditableCurve? {
            return curves?.minBy { it.distanceTo(pos) }
        }

        fun addCurve() {
            var c = EditableCurve()
            curves.add(c)

            activeCurve = curves.lastOrNull()

            c.randomize(drawer)
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
                        curves.remove(activeCurve)
                        activeCurve = curves.lastOrNull()
                        //curves.removeAt(activeCurve)
                        //activeCurve = activeCurve.coerceIn(0, curves.size - 1)
                    }
                }
                button {
                    label = "Clear"
                    clicked {
                        segments.clear()
                        curves.clear()
                        //activeCurve = curves.size - 1
                        activeCurve = null
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
                        activeCurve?.setNumSubcurves(it.newValue.toInt())
                        refreshCurves()
                    }
                }
                paramSep = slider {
                    label = "Separation"
                    value = 1.0
                    range = Range(-50.0, 50.0)
                    precision = 0
                    events.valueChanged.subscribe {
                        activeCurve?.setSep(it.newValue.toInt())
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
            curves.forEach {
                it.draw(drawer, it == activeCurve)
            }

            keyboard.keyDown.listen {
                if (it.key == KEY_ESCAPE) {
                    exitProcess(0)
                }
            }

            mouse.buttonDown.listen {
                activeCurve?.mousePressed(it.position)
                mouseClickStart = it.position;

            }

            mouse.dragged.listen { mouse ->
                activeCurve?.let {
                    it.mouseDragged(
                        Vector2(
                            clamp(mouse.position.x, 0.0, width.toDouble()),
                            clamp(mouse.position.y, 0.0, height.toDouble())
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
                    activeCurve?.let {
                        paramNumSubcurves.value = it.getNumSubcurves().toDouble()
                        paramSep.value = it.getSep().toDouble()
                    }
                }
                if (curvesNeedUpdate) {
                    refreshCurves()
                    curvesNeedUpdate = false
                }
            }
        }
    }
}