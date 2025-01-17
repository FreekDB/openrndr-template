package apps.editablecurve

import aBeLibs.extensions.FPSDisplay
import aBeLibs.extensions.NoJitter
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.dialogs.saveFileDialog
import org.openrndr.draw.loadFont
import org.openrndr.extra.midi.MidiDeviceDescription
import org.openrndr.extra.midi.MidiTransceiver
import org.openrndr.math.IntVector2
import org.openrndr.shape.CompositionDrawer
import org.openrndr.svg.writeSVG

/**
 * id: 80aeb6c0-9a60-4933-9629-8ae7e91ed7d1
 * description: New sketch
 * tags: #new
 */

fun main() = application {
    configure {
        width = 900
        height = 900
        position = IntVector2(10, 10)
    }

    program {
        EditableCurve.colorEdit = ColorRGBa(0.5, 0.8, 0.9)

        val font = loadFont("/home/funpro/.local/share/fonts/NovaMono.ttf", 20.0)


        // MidiFighter
        val mf = try {
            MidiDeviceDescription.list().forEach {
                println("${it.name}, ${it.vendor} r:${it.receive} t:${it.transmit}")
            }
            MidiTransceiver.fromDeviceVendor("Twister [hw:2,0,0]", "ALSA (http://www.alsa-project.org)")
        } catch (e: IllegalArgumentException) {
            null
        }
        mf?.controlChanged?.postpone = true
        mf?.controlChanged?.listen {
            when (it.control) {
                12 -> {
                    EditableCurveState.activeCurve?.numSubcurves = it.value / 4
                    EditableCurveState.refreshCurves()
                }
                13 -> {
                    EditableCurveState.activeCurve?.separation = it.value - 64.0
                    EditableCurveState.refreshCurves()
                }
                else -> println("${it.channel} ${it.control} ${it.value}")
            }
        }

        // Init ECState
        EditableCurveState.winSize = window.size
        window.sized.listen {
            EditableCurveState.winSize = it.size
        }

        extend(NoJitter())
        extend(setupUI())
        //extend(ScreenRecorder())
        extend(FPSDisplay(font, ColorRGBa.BLACK))

        extend {
            // midi events
            mf?.controlChanged?.deliver()

            // clear
            drawer.clear(EditableCurveState.bgColor)
            drawer.fontMap = font

            // export SVG
            if (EditableCurveState.saveSVG) {
                exportSVG()
                EditableCurveState.saveSVG = false
            }

            // Draw everything
            EditableCurveState.segments.forEach { drawer.contour(it) }
            EditableCurveState.curves.forEach { curve ->
                curve.draw(drawer, curve == EditableCurveState.activeCurve)
            }

            // Interaction: keyboard
            keyboard.keyDown.listen {
                //mf.controlChange(0, Random.int0(16), Random.int0(128))
            }

            // Interaction: mouse
            mouse.buttonDown.listen {
                EditableCurveState.onMouseDown(it.position)
            }

            mouse.dragged.listen {
                EditableCurveState.onMouseDrag(it.position)
            }

            mouse.buttonUp.listen {
                EditableCurveState.onMouseUp(it.position)
            }
        }
    }
}

fun exportSVG() {
    val svg = CompositionDrawer()

    svg.fill = null
    svg.stroke = ColorRGBa.BLACK

    EditableCurveState.segments.forEach { svg.contour(it) }

    saveFileDialog(supportedExtensions = listOf("svg")) {
        it.writeText(writeSVG(svg.composition))
    }
}
