package apps2.simpleTests

import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer

/**
 * id: b6714624-f9c7-4721-b17c-35896c43094a
 * description: New sketch
 * tags: #new
 */

class SimpleDrawingClass(val drawer: Drawer) {
    fun draw() {
        with(drawer) {
            strokeWeight = 8.0
            stroke = ColorRGBa.GREEN
            circle(0.0, 0.0, 100.0)
        }
    }
}
