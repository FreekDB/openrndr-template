import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.shape.Circle

/**
 * id: 9548b935-1c31-47cf-b718-bb6ad1320130
 * description: New sketch
 * tags: #new
 */

fun main() = application {
    configure { }
    program {
        extend {
            val a = Circle(drawer.bounds.center, 50.0)
            val b = Circle(mouse.position, 150.0)

            val linesOuter = a.tangents(b, false)
            val linesInner = a.tangents(b, true)

            if (keyboard.pressedKeys.contains("a")) {
                println()
                println(linesOuter)
                println(linesInner)
            }

            drawer.stroke = null
            drawer.fill = ColorRGBa.GRAY
            drawer.circle(a)
            drawer.circle(b)

            drawer.strokeWeight = 4.0
            drawer.stroke = ColorRGBa.RED
            linesOuter.forEach {
                drawer.lineSegment(it.first, it.second)
            }

            drawer.strokeWeight = 2.0
            drawer.stroke = ColorRGBa.YELLOW
            linesInner.forEach {
                drawer.lineSegment(it.first, it.second)
            }
        }
    }
}
