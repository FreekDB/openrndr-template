import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import kotlin.system.exitProcess

/**
 * March 2020
 *
 * Build something that allows me to rotate, translate and scale objects
 * like in Inkscape. Maybe I need a Draggable class. That class keeps
 * a transformation matrix which I can adjust with the mouse.
 *
 * It provides a method to query if the mouse is on top of the object.
 *
 * The method would check a pixel in a 1 bit buffer to know if we have
 * clicked on the drawing. But would that buffer resize when scaling the object?
 *
 * So it has two buffers? RGBa buffer for rendering and 1-bit mask for mouse clicks?
 * That's good if I'm dragging layers around to not re-render them.
 *
 * Next steps:
 * - [] Make selectable rectangles
 */

fun main() = application {
    configure {
        width = 768
        height = 576
    }

    program {

        val rect = Rectangle.fromCenter(Vector2.ZERO,200.0, 50.0)

        extend {
            drawer.rectangle(rect)
        }

        keyboard.keyDown.listen {
            when (it.key) {
                KEY_ESCAPE -> exitProcess(0)
            }
        }

    }
}
