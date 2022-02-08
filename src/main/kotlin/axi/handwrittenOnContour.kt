package axi

import aBeLibs.extensions.Handwritten
import org.openrndr.KEY_ENTER
import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.dialogs.saveFileDialog
import org.openrndr.draw.LineJoin
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.Random
import org.openrndr.extra.shapes.hobbyCurve
import org.openrndr.math.Vector2
import org.openrndr.shape.CompositionDrawer
import org.openrndr.shape.ShapeContour
import org.openrndr.svg.writeSVG
import kotlin.system.exitProcess

/**
 * Converts text to handwritten text and makes it follow a contour
 */

fun main() = application {

    program {
        val handwritten = Handwritten()

        extend(handwritten)

        """
ABSTRACT
Many physicists would agree that, had it not been for
congestion control, the evaluation of web browsers might never
have occurred. In fact, few hackers worldwide would disagree
with the essential unification of voice-over-IP and public-
private key pair. In order to solve this riddle, we confirm that
SMPs can be made stochastic, cacheable, and interposable.
I. INTRODUCTION 
Many scholars would agree that, had it not been for active
networks, the simulation of Lamport clocks might never have
occurred. The notion that end-users synchronize with the
investigation of Markov models is rarely outdated. A theoretical 
grand challenge in theory is the important unification
of virtual machines and real-time theory. To what extent can
web browsers be constructed to achieve this purpose?
Certainly, the usual methods for the emulation of Smalltalk
that paved the way for the investigation of rasterization do
not apply in this area. In the opinions of many, despite the
fact that conventional wisdom states that this grand challenge
is continuously answered by the study of access points, we
believe that a different solution is necessary.
""".split("\n").forEach { l ->
            handwritten.add(l, Vector2.ZERO)
        }

        val svg = CompositionDrawer()
        svg.fill = null
        svg.stroke = ColorRGBa.BLACK
        svg.strokeWeight = 0.5
        handwritten.drawToSVG(svg, List(handwritten.lineCount) { y ->
            ShapeContour.fromPoints(
                List(40) { x ->
                    val pc = x / 40.0
                    drawer.bounds.position(
                        0.15 + 0.8 * pc,
                        0.15 + 0.7 * y / handwritten.lineCount +
                                (0.05 + 0.05 * pc) * Random.simplex(x * 0.05,
                            y * 0.1)
                    )
                }, false
            ).hobbyCurve()
        })

        fun exportSVG() {
            saveFileDialog(supportedExtensions = listOf("svg")) {
                it.writeText(writeSVG(svg.composition))
            }
        }

        extend(Screenshots())
        extend {
            drawer.run {
                clear(ColorRGBa.WHITE)
                lineJoin = LineJoin.BEVEL
                composition(svg.composition)
            }
        }

        keyboard.keyDown.listen {
            when (it.key) {
                KEY_ESCAPE -> exitProcess(0)
                KEY_ENTER -> exportSVG()
            }
        }
    }
}
