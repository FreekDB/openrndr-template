package apps2.simpleTests

import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer

class SimpleDrawingClass(val drawer: Drawer) {
    fun draw() {
        with(drawer) {
            strokeWeight = 8.0
            stroke = ColorRGBa.GREEN
            circle(0.0, 0.0, 100.0)
        }
    }
}