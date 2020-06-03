package apps

import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extensions.Screenshots
import org.openrndr.math.Vector2
import org.openrndr.shape.LineSegment
import org.openrndr.shape.intersection
import kotlin.system.exitProcess

/**
 * Basic template
 */

fun main() = application {
    configure {
        width = 1500
        height = 800
    }

    program {
        val s = listOf(
            LineSegment(Vector2(766.7664464471056, 452.5984489875642), Vector2(753.577266431925, 452.5984489875639)),
            LineSegment(Vector2(753.577266431925, 452.5984489875639), Vector2(741.4490637297909, 431.5917857029747)),
            LineSegment(Vector2(741.4490637297909, 431.5917857029747), Vector2(761.0097317710685, 397.7117148254943)),
            LineSegment(Vector2(761.0097317710685, 397.7117148254943), Vector2(779.7325244807926, 430.1405430583163)),
            LineSegment(Vector2(779.7325244807926, 430.1405430583163), Vector2(766.7664464471056, 452.5984489875642))
            )
        val knife = LineSegment(Vector2(3760.629961242148, 429.92128244121415), Vector2(-2239.370038757852, 429.92128244121415))

        s.forEachIndexed { id, other ->
            print("$id: ")
            println( intersection(other, knife))
        }

        extend(Screenshots())
        extend {
            drawer.stroke = ColorRGBa.GREEN
            drawer.lineSegment(knife)
            drawer.stroke = ColorRGBa.RED
            s.forEach {
                drawer.lineSegment(it)
            }
        }

        keyboard.keyDown.listen {
            when (it.key) {
                KEY_ESCAPE -> exitProcess(0)
            }
        }
    }
}
