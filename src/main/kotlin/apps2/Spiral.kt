package apps2

import aBeLibs.geometry.circler
import aBeLibs.geometry.spiralContour
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extensions.Screenshots
import org.openrndr.math.Vector2
import org.openrndr.shape.Circle

/**
 * id: ce3c4813-d9a7-4cd3-a878-791ceff3e85a
 * description: Create a spiral connecting two points
 * in two concentric circles. Interactive.
 * tags: #spiral
 */

fun main() = application {
    program {
        var p0 = Vector2(100.0, 100.0)
        var p1 = Vector2(300.0, 300.0)
        var center = drawer.bounds.center

        extend(Screenshots())
        extend {
            drawer.run {
                clear(ColorRGBa.WHITE)
                fill = null
                stroke = ColorRGBa.GRAY
                lineSegment(p0, center)
                lineSegment(p1, center)
                circler(Circle(center, center.distanceTo(p0)))
                circler(Circle(center, center.distanceTo(p1)))

                stroke = ColorRGBa.BLACK
                contour(
                    spiralContour(
                        p0,
                        p1,
                        center,
                        if (keyboard.pressedKeys.contains("left-shift")) 2 else -2
                    )
                )
            }
        }

        mouse.dragged.listen {
            when (listOf(p0, p1, center).minByOrNull { p ->
                p.squaredDistanceTo(it.position)
            }) {
                p0 -> p0 = it.position
                p1 -> p1 = it.position
                center -> center = it.position
            }
        }
    }
}
