package latest

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

/**
 * Making PanZoomCanvas work for non 1:1 aspect ratio.
 * Unfinished. The working version was accidentally lost.
 */
fun main() = application {
    configure {
        width = 600
        height = 400
        hideWindowDecorations = true
        position = IntVector2(10, 10)
    }

    program {
        val canvas = PanZoomCanvas(
            this, 900, 900,
            ColorRGBa.PINK
        )
        canvas.viewport = Rectangle(
            100.0, 100.0, 400.0, 200.0
        )

        extend {
            drawer.isolatedWithTarget(canvas.rt) {
                strokeWeight = 5.0
                stroke = if (Random.nextBoolean()) ColorRGBa.BLACK else ColorRGBa.WHITE
                val angle = frameCount.toDouble()
                ortho(canvas.rt)
                lineSegment(
                    canvas.center + Polar(angle, 200.0).cartesian,
                    canvas.center + Polar(angle, 450.0).cartesian
                )
            }
            canvas.draw()
        }

        keyboard.keyDown.listen {
            when (it.key) {
                KEY_ESCAPE -> application.exit()
                KEY_SPACEBAR -> {
                    val pos = canvas.globalToLocal(mouse.position)
                    drawer.isolatedWithTarget(canvas.rt) {
                        ortho(canvas.rt)
                        drawer.circle(pos, 20.0)
                    }
                }
            }
        }
    }
}
