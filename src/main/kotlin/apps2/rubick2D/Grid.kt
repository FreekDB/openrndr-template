package apps2.rubick2D

import aBeLibs.data.CircularArray
import org.openrndr.draw.Drawer
import org.openrndr.extra.noise.Random
import kotlin.math.sign

/**
 * id: e1f1e468-c08a-4019-88ca-f9b3241c878d
 * description: New sketch
 * tags: #new
 */

class Grid {
    private val grid = List(columns * rows) { Cell(it) }
    private val history = CircularArray(4, Pair(grid[0], grid[0]))

    init {
        grid.forEach { it.populateNeighbors(grid) }
        populateCells()
    }

    private fun populateCells() {
        grid.indices.shuffled().subList(0, cellCount).forEachIndexed { i, n ->
            grid[n].type = i
        }
    }

    fun moveOne() {
        val cell = grid.filter { it.isOccupied() }.random()
        cell.neighbors().filter { it.isEmpty() }.randomOrNull()?.run {
            copyTypeFrom(cell)
            cell.clear()
        }
    }

    fun moveSome() {
        var found = false
        do {
            // Find occupied cell
            val selection = mutableListOf(grid.filter { it.isOccupied() }.random())
            selection[0].neighbors().filter { it.isEmpty() }.randomOrNull()?.apply {
                // Insert neighboring empty at the beginning of the selection
                selection.add(0, this)

                // Now extend selection in direction empty->occupied
                val dir = selection[1].dirTo(selection[0])
                var next = selection.last().neighbor(dir)
                while (next?.isOccupied() == true && Random.bool(0.8)) {
                    selection.add(next)
                    next = next.neighbor(dir)
                }

                // Go through history checking that there is no overlap
                // between selection and one of the previous move
                // in the same direction

                val currentRange = Pair(selection.first(), selection.last())

                if (history.none { it.isCounterMove(currentRange) }) {
                    found = true

                    // Add pair to history, skipping the last cell which will become empty.
                    history.put(currentRange)

                    // Move selected cells towards the empty
                    (1 until selection.size).forEach {
                        selection[it - 1].copyTypeFrom(selection[it])
                        selection[it - 1].animateFrom(selection[it])
                    }
                    // Empty the last selected cell
                    selection.last().clear()
                }
            }
        } while (!found)
    }

    fun draw(drawer: Drawer) {
        grid.forEach {
            it.draw(drawer)
        }
    }
}

typealias CellRange = Pair<Cell, Cell>

private fun CellRange.hasWidth(): Boolean = first.x() != second.x()
private fun CellRange.hasHeight(): Boolean = first.y() != second.y()

// Note: it doesn't actually calculate motion overlap, but motion
// direction in the same row or column.
private fun CellRange.isCounterMove(other: CellRange): Boolean {
    val cells = listOf(first, second, other.first, other.second)
    // horizontal
    if (hasWidth() && other.hasWidth()) {
        if (cells.all { it.y() == cells.first().y() }) {
            return (first.x() - second.x()).sign !=
                    (other.first.x() - other.second.x()).sign
        }
    }
    // vertical
    if (hasHeight() && other.hasHeight()) {
        if (cells.all { it.x() == cells.first().x() }) {
            return (first.y() - second.y()).sign !=
                    (other.first.y() - other.second.y()).sign
        }
    }
    return false
}
