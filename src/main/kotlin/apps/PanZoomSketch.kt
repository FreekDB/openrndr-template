package apps2

import aBeLibs.panzoom.PanZoomCanvas
import org.openrndr.KEY_ESCAPE
import org.openrndr.KEY_SPACEBAR
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.math.IntVector2
import org.openrndr.math.Polar
import org.openrndr.shape.Rectangle
import kotlin.random.Random

fun main() = application {
    configure {
        width = 600
        height = 400
        hideWindowDecorations = true
        position = IntVector2(10, 10)
    }

    program {
        val columns = 3
        val rows = 2
        val w = width.toDouble() / columns
        val h = height.toDouble() / rows
        val canvases = List(6) {
            val canvas = PanZoomCanvas(
                this, 900, 900,
                ColorRGBa.PINK
            )
            canvas.viewport = Rectangle(
                (it % columns) * w,
                (it / columns) * h, w, h
            )

            canvas
        }

        extend {
            // Paint in one canvas per frame
            val currCanvas = canvases[frameCount % canvases.size]
            drawer.isolatedWithTarget(currCanvas.rt) {
                strokeWeight = 5.0
                stroke = if (Random.nextBoolean()) ColorRGBa.BLACK else ColorRGBa.WHITE
                val angle = frameCount.toDouble()
                ortho(currCanvas.rt)
                lineSegment(
                    currCanvas.center + Polar(angle, 200.0).cartesian,
                    currCanvas.center + Polar(angle, 400.0).cartesian
                )
            }
            // Show them all
            canvases.forEach { it.draw() }
        }

        keyboard.keyDown.listen {
            when (it.key) {
                KEY_ESCAPE -> application.exit()
                KEY_SPACEBAR -> {
                    canvases.firstOrNull { it.active }?.apply {
                        drawer.isolatedWithTarget(rt) {
                            ortho(rt)
                            drawer.circle(globalToLocal(mouse.position), 20.0)
                        }
                    }
                }
            }
        }
    }
}
