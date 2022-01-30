package latest

import aBeLibs.geometry.bentFromPoints
import org.openrndr.KEY_ESCAPE
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
import org.openrndr.svg.writeSVG


/**
 * A PlantLeaf contains a 2D List of EditableDot.
 * A PlantLeaf has a build() method that creates a list of [Segment] based on
 * those dots.
 */
fun main() = application {
    configure {
        width = 800
        height = 800
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
            if (it.key == KEY_ESCAPE) {
                application.exit()
            }
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

class PlantLeaf(private val drawer: Drawer) {
    val points = MutableList(15) {
        val x = it % 3
        val y = it / 3
        drawer.bounds.position(
            x.toDouble().map(0.0, 2.0, 0.25, 0.75),
            y.toDouble().map(0.0, 4.0, 0.1, 0.9)
        )
    }
    var currPointEdit = -1
    var dragStartPos = Vector2.ZERO
    val curves = mutableListOf<Segment>()


    private fun addCurves(ids: List<Int>) {
        val (a, b, c, d) = ids.map { points[it] }
        val rnd = 0.5
        val sourceContour = ShapeContour.bentFromPoints(a, b, rnd).contour +
                ShapeContour.bentFromPoints(b, c, rnd).contour +
                ShapeContour.bentFromPoints(c, d, rnd).contour
        val closingContour = ShapeContour.bentFromPoints(a, d, rnd)
        val curvedSourceContour = Segment(a, b, c, d)

        val steps = (sourceContour.length / 5).toInt()
        val equiPoints0 = sourceContour.equidistantPositions(steps)
        val equiPoints1 = closingContour.equidistantPositions(steps)

        equiPoints0.forEachIndexed { id, p0 ->
            val u = id / steps.toDouble()
            val p1 = equiPoints1[id]
            val p0n = curvedSourceContour.normal(u)
            val p1n = closingContour.normal(u)
            val dist = p0.distanceTo(p1)
            val s = Segment(
                p0, p0 - p0n * dist * 0.5, p1 + p1n * dist * 0.5, p1
            )
            curves.add(s.sub(0.05, 1.0))
        }
    }

    /**
     * Converts [points] into a list of [Segment]
     */
    fun build() {
        curves.clear()
        for (y in 0 until points.size - 3 step 3) {
            addCurves(listOf(y + 0, y + 1, y + 4, y + 3))
            addCurves(listOf(y + 5, y + 4, y + 1, y + 2))
        }
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
