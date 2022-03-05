package apps

import aBeLibs.geometry.angleToSquare
import aBeLibs.geometry.fromIrregularLine
import org.openrndr.KEY_ENTER
import org.openrndr.KEY_ESCAPE
import org.openrndr.KEY_INSERT
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.dialogs.saveFileDialog
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.Random
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.shape.CompositionDrawer
import org.openrndr.shape.shape
import org.openrndr.svg.writeSVG
import kotlin.system.exitProcess

/**
 * id: 619d0b78-df19-4a67-aae6-3e0963458c16
 * description: Wavy lines connecting a central circle with the window bounds
 * tags: #axidraw
 */

fun main() = application {
    configure {
        width = (800 * 1.3).toInt()
        height = 800
    }

    program {
        extend(Screenshots())

        var lines = shape {}

        fun populate() {
            Random.seed = Math.random().toString()
            lines = shape {
                (0 until 360 step 2).forEach { angle ->
                    val p0 = Polar(angle + 30.0, 100.0).cartesian
                    val p1 = angleToSquare(angle.toDouble(), 380.0)
                    contour {
                        moveTo(p0)
                        (1..200).forEach {
                            lineTo(fromIrregularLine(p0, p1 * Vector2(1.3, 1.0), it / 200.0))
                        }
                    }
                }
            }
        }

        fun exportSVG() {
            val svg = CompositionDrawer()

            svg.fill = null
            svg.stroke = ColorRGBa.BLACK
            svg.shape(lines)
            saveFileDialog(supportedExtensions = listOf("svg")) {
                it.writeText(writeSVG(svg.composition))
            }
        }

        populate()

        extend {
            drawer.translate(drawer.bounds.center)
            drawer.clear(ColorRGBa.WHITE)
            drawer.stroke = ColorRGBa(0.0, 0.0, 0.0, 0.5)
            drawer.fill = null
            drawer.shape(lines)
        }

        keyboard.keyDown.listen {
            when (it.key) {
                KEY_ESCAPE -> exitProcess(0)
                KEY_ENTER -> populate()
                KEY_INSERT -> exportSVG()
            }
        }
    }
}
