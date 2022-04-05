package latest

import aBeLibs.geometry.bentFromPoints
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.dialogs.saveFileDialog
import org.openrndr.draw.Drawer
import org.openrndr.draw.isolated
import org.openrndr.extensions.Screenshots
import org.openrndr.math.Vector2
import org.openrndr.math.map
import org.openrndr.panel.controlManager
import org.openrndr.panel.elements.button
import org.openrndr.panel.elements.div
import org.openrndr.panel.style.*
import org.openrndr.shape.CompositionDrawer
import org.openrndr.shape.Segment
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.intersections
import org.openrndr.svg.writeSVG
import java.util.*

/**
 * id: 3265dce2-f96a-4486-bc01-5e9215da9c25
 * description: New sketch
 * tags: #new
 */

/**
 * A PlantLeaf contains a 2D List of EditableDot.
 * A PlantLeaf has a build() method that creates a list of [Segment] based on
 * those dots.
 * [image](/home/funpro/OR/openrndr-template/screenshots/latest.PlantLeaf-2022-02-01-20.40.33.png)
 */
fun main() = application {
    configure {
        width = 1100
        height = 1100
    }

    program {
        val leaves = mutableListOf<PlantLeaf>()
        var editingLeaf: PlantLeaf? = null

        lateinit var horiz: StyleSheet
        val ui = controlManager {
            horiz = styleSheet(has class_ "horizontal") {
                paddingLeft = 10.px
                paddingTop = 10.px
                display = Display.FLEX
                flexDirection = FlexDirection.Row
                width = 100.percent
            }

            layout {
                div("horizontal") {
                    button(label = "add Leaf") {
                        events.clicked.listen { leaves.add(PlantLeaf(drawer)) }
                    }
                    button(label = "clear") {
                        events.clicked.listen {
                            leaves.clear()
                        }
                    }
                    button(label = "save SVG") {
                        events.clicked.listen {
                            val svg = CompositionDrawer()
                            svg.stroke = ColorRGBa.BLACK
                            leaves.forEach { leaf ->
                                svg.contours(leaf.curves.map { it.contour })
                            }
                            saveFileDialog(supportedExtensions = listOf("svg")) {
                                it.writeText(writeSVG(svg.composition))
                            }
                        }
                    }
                    button(label = "exit") {
                        events.clicked.listen { application.exit() }
                    }
                }
            }
        }
        extend(ui)
        extend(Screenshots())
        extend {
            drawer.clear(ColorRGBa.WHITE)
            leaves.forEach { it.draw() }
        }

        keyboard.keyDown.listen {
            if (it.name == "h") {
                horiz.display = if (horiz.display == Display.NONE)
                    Display.FLEX else Display.NONE
            }
        }

        mouse.buttonDown.listen {
            if (!it.propagationCancelled) {
                editingLeaf = leaves.find { leaf ->
                    leaf.isUnderMouse(it.position)
                }
            }
        }
        mouse.buttonUp.listen {
            editingLeaf?.dragEnd()
            editingLeaf = null
        }
        mouse.dragged.listen {
            editingLeaf?.drag(it.position)
        }
    }
}

/**
 * Editable grid of points. The point form rectangular shapes.
 * Each one is filled with curves
 */
class PlantLeaf(private val drawer: Drawer) {
    private val cols = 3
    private val rows = 5
    val points = MutableList(cols * rows) {
        val x = it % cols
        val y = it / cols
        drawer.bounds.position(
            x.toDouble().map(0.0, cols - 1.0, 0.2, 0.8),
            y.toDouble().map(0.0, rows - 1.0, 0.1, 0.9)
        )
    }
    private var currPointEdit = -1
    private var dragStartPos = Vector2.ZERO
    val curves = mutableListOf<Segment>()

    private fun addCurves(ids: List<Int>) {
        val (a, b, c, d) = ids.map { points[it] }
        val rnd = 0.5
        val sourceContour = ShapeContour.bentFromPoints(a, b, rnd).contour +
                ShapeContour.bentFromPoints(b, c, rnd).contour +
                ShapeContour.bentFromPoints(c, d, rnd).contour
        val closingContour = ShapeContour.bentFromPoints(a, d, rnd)
        val curvedSourceContour = Segment(a, b, c, d)

        // density
        val steps = (sourceContour.length / 5).toInt()
        val equiPoints0 = sourceContour.equidistantPositions(steps)
        val equiPoints1 = closingContour.equidistantPositions(steps)

        equiPoints0.forEachIndexed { id, p0 ->
            val u = id / steps.toDouble()
            val p1 = equiPoints1[id]
            val p0n = curvedSourceContour.normal(u)
            val p1n = closingContour.normal(u)
            val dist = p0.distanceTo(p1)
            val force0 = 0.2
            val force1 = 0.8
            val s = Segment(
                p0, p0 - p0n * dist * force0, p1 + p1n * dist * force1, p1
            )
            //curves.add(s)
            curves.add(s.sub(0.01, 0.99))
        }
    }

    /**
     * Converts [points] into a list of [Segment]
     */
    fun build() {
        curves.clear()
        for (y in 0 until points.size - cols step cols) {
            for (x in 0 until cols - 1) {
                val b = x + y
                val a = mutableListOf(b, b + 1, b + 1 + cols, b + cols)
                Collections.rotate(a, (x % 2) * 2 + y / (cols * 3))
                addCurves(a)
            }
        }
        //println(countIntersections(curves))
    }

    init {
        build()
    }

    fun draw() {
        drawer.isolated {
            stroke = null
            fill = ColorRGBa.BLACK.opacify(0.2)
            circles(points, 10.0)

            stroke = ColorRGBa.BLACK.opacify(0.8)
            segments(curves)
        }
    }

    fun isUnderMouse(pos: Vector2): Boolean {
        currPointEdit = points.withIndex().minByOrNull {
            it.value.squaredDistanceTo(pos)
        }?.index ?: -1
        dragStartPos = pos
        return currPointEdit >= 0
    }

    fun dragEnd() {
        build()
    }

    fun drag(pos: Vector2) {
        points[currPointEdit] = pos
    }
}

/**
 * Count intersections in a list of Segment
 */
private fun countIntersections(c: List<Segment>): Int {
    var total = 0
    for (i in c.indices) {
        for (j in i + 1 until c.size) {
            if (c[i].intersections(c[j]).isNotEmpty()) {
                total++
            }
        }
    }
    return total
}
