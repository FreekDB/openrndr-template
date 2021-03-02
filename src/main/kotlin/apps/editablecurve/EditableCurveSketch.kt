package apps.editablecurve

import aBeLibs.extensions.FPSDisplay
import aBeLibs.extensions.NoJitter
import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.dialogs.saveFileDialog
import org.openrndr.draw.loadFont
import org.openrndr.extra.midi.MidiDeviceDescription
import org.openrndr.extra.midi.MidiTransceiver
import org.openrndr.math.IntVector2
import org.openrndr.shape.CompositionDrawer
import org.openrndr.svg.writeSVG
import kotlin.system.exitProcess

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
                    ECState.activeCurve?.numSubcurves = it.value / 4
                    ECState.refreshCurves()
                }
                13 -> {
                    ECState.activeCurve?.separation = it.value - 64.0
                    ECState.refreshCurves()
                }
                else -> println("${it.channel} ${it.control} ${it.value}")
            }
        }

        // Init ECState
        ECState.winSize = window.size
        window.sized.listen {
            ECState.winSize = it.size
        }

        extend(NoJitter())
        extend(setupUI())
        //extend(ScreenRecorder())
        extend(FPSDisplay(font))

        extend {
            // midi events
            mf?.controlChanged?.deliver()

            // clear
            drawer.clear(ECState.bgColor)
            drawer.fontMap = font

            // ---------------------------------
            // export SVG
            if (ECState.saveSVG) {
                exportSVG()
                ECState.saveSVG = false
            }

            // ----------------------------------
            // Draw everything
            ECState.segments.forEach { drawer.contour(it) }
            ECState.curves.forEach { curve ->
                curve.draw(drawer, curve == ECState.activeCurve)
            }

            // ----------------------------------
            // Interaction: keyboard
            keyboard.keyDown.listen {
                if (it.key == KEY_ESCAPE) {
                    exitProcess(0)
                }
                //mf.controlChange(0, Random.int0(16), Random.int0(128))
            }

            // -----------------------------------
            // Interaction: mouse
            mouse.buttonDown.listen {
                ECState.onMouseDown(it.position)
            }

            mouse.dragged.listen {
                ECState.onMouseDrag(it.position)
            }

            mouse.buttonUp.listen {
                ECState.onMouseUp(it.position)
            }
        }
    }
}

fun exportSVG() {
    val svg = CompositionDrawer()

    svg.fill = null
    svg.stroke = ColorRGBa.BLACK

    ECState.segments.forEach { svg.contour(it) }

    saveFileDialog(supportedExtensions = listOf("svg")) {
        it.writeText(writeSVG(svg.composition))
    }
}

//fun File.ensureExtension(ext: String): File {
//    return if (this.absolutePath.toLowerCase().endsWith(ext)) {
//        this
//    } else {
//        File(this.absolutePath + ext)
//    }
//}
