import org.openrndr.KEY_ESCAPE
import org.openrndr.KEY_SPACEBAR
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.shape.Rectangle
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

fun main() = application {
    configure {
        width = 600
        height = 400
    }

    program {
        val columns = 3
        val rows = 2
        val w = width.toDouble() / columns
        val h = height.toDouble() / rows
        val canvases = List(6) {
            val c = PanZoomCanvas(900, 900)
            c.setViewport(Rectangle((it % 3) * w, (it / 3) * h, w, h))
            drawer.isolatedWithTarget(c.rt) {
                drawer.background(ColorRGBa.PINK)
            }
            c
        }
        var activeCanvas: PanZoomCanvas? = canvases[0]


        extend {
            // Paint in one canvas per frame
            val canvasID = frameCount % canvases.size
            val currCanvas = canvases[canvasID]
            drawer.isolatedWithTarget(currCanvas.rt) {
                drawer.strokeWeight = 5.0
                drawer.stroke = if(Random.nextBoolean()) ColorRGBa.BLACK else ColorRGBa.WHITE
                val angle = frameCount.toDouble()
                ortho(currCanvas.rt)
                drawer.lineSegment(
                    currCanvas.rt.width * 0.5 + 200 * cos(angle),
                    currCanvas.rt.height * 0.5 + 200 * sin(angle),
                    currCanvas.rt.width * 0.5 + 400 * cos(angle),
                    currCanvas.rt.height * 0.5 + 400 * sin(angle)
                )
            }
            // Show them all
            canvases.forEach { it.draw(drawer) }
        }

        mouse.moved.listen {
            activeCanvas = canvases.firstOrNull { it.inside(mouse.position) }
        }

        mouse.scrolled.listen {
            activeCanvas?.wheel(it)
        }

        mouse.dragged.listen {
            activeCanvas?.drag(it)
        }

        keyboard.keyDown.listen {
            when (it.key) {
                KEY_ESCAPE -> application.exit()
                KEY_SPACEBAR -> {
                    activeCanvas?.run {
                        val canvas = this
                        drawer.isolatedWithTarget(canvas.rt) {
                            ortho(canvas.rt)
                            drawer.circle(canvas.globalToLocal(mouse.position), 20.0)
                        }
                    }
                }
            }
        }
    }
}
