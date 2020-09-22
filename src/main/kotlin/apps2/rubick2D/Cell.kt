package apps2.rubick2D

import org.openrndr.animatable.Animatable
import org.openrndr.animatable.easing.Easing
import org.openrndr.color.ColorHSLa
import org.openrndr.draw.Drawer
import org.openrndr.math.IntVector2
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import kotlin.math.abs

private val style = object {
    val margin = 5.0
    val size = Vector2(128.0, 64.0)
    val gridOffset = IntVector2(-2, -2)
}

class Vector2Anim() : Animatable() {
    var x: Double = 0.0
    var y: Double = 0.0
}

data class Cell(
    val id: Int,
    var type: Int = -1,
    val gridPos: IntVector2 = IntVector2(id % columns, id / columns)
) {
    private val screenPos = (style.size + style.margin) * (gridPos + style.gridOffset).vector2
    private var screenPosOffset = Vector2Anim()

    private var up: Cell? = null
    private var down: Cell? = null
    private var left: Cell? = null
    private var right: Cell? = null

    fun x() = gridPos.x
    fun y() = gridPos.y
    fun isEmpty() = type < 0
    fun isOccupied() = type >= 0
    fun neighbors() = listOfNotNull(up, down, left, right)
    fun neighbor(dir: IntVector2): Cell? {
        return when (dir) {
            IntVector2(1, 0) -> right
            IntVector2(-1, 0) -> left
            IntVector2(0, 1) -> down
            else -> up
        }
    }

    fun draw(drawer: Drawer) {
        if (isOccupied()) {
            screenPosOffset.updateAnimation()
            val pos = screenPos + Vector2(screenPosOffset.x, screenPosOffset.y)
            val hue = (40 + type * 120) % 360.0
            drawer.strokeWeight = 2.0
            drawer.stroke = ColorHSLa(hue, 0.7, 0.35).toRGBa()

            drawer.fill = ColorHSLa(hue, 0.7, 0.7).toRGBa()
            drawer.rectangle(Rectangle.fromCenter(pos, style.size.x, style.size.y))

            drawer.fill = ColorHSLa(hue, 0.7, 0.2).toRGBa()
            drawer.text(type.toString(), pos)
        }
    }

    fun clear() {
        type = -1
    }

    fun populateNeighbors(grid: List<Cell>) {
        if (gridPos.x > 0) {
            left = grid[id - 1]
        }
        if (gridPos.x < columns - 1) {
            right = grid[id + 1]
        }
        if (gridPos.y > 0) {
            up = grid[id - columns]
        }
        if (gridPos.y < rows - 1) {
            down = grid[id + columns]
        }
    }

    fun copyTypeFrom(other: Cell) {
        type = other.type
    }

    fun dirTo(other: Cell) = gridPos - other.gridPos
    fun animateFrom(other: Cell) {
        screenPosOffset.run {
            cancel()
            x = other.screenPos.x - screenPos.x
            y = other.screenPos.y - screenPos.y
            if (abs(x) > 0.01) {
                animate("x", 0.0, 150, Easing.QuadInOut)
            }
            if (abs(y) > 0.01) {
                animate("y", 0.0, 150, Easing.QuadInOut)
            }
        }
    }

    fun gridPosEquals(other: Cell) = gridPos == other.gridPos

}