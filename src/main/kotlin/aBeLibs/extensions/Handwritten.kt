package aBeLibs.extensions

import org.openrndr.Extension
import org.openrndr.Program
import org.openrndr.draw.Drawer
import org.openrndr.draw.LineJoin
import org.openrndr.draw.isolated
import org.openrndr.math.Vector2
import org.openrndr.math.transforms.transform
import org.openrndr.shape.CompositionDrawer
import org.openrndr.shape.Rectangle
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.bounds
import org.openrndr.svg.loadSVG
import kotlin.math.max

class Handwritten : Extension {
    override var enabled: Boolean = true

    private val svg = loadSVG("data/text-template-3.svg")
    private val bounds = mutableMapOf<Char, Rectangle>()
    private val letters = mutableMapOf<Char, MutableList<ShapeContour>>()
    private val lines = mutableListOf<Pair<String, Vector2>>()
    var scale = 1.0

    fun add(txt: String, pos: Vector2, align: Vector2 = Vector2.ZERO) {
        var size = Vector2.ZERO
        txt.forEach { letter ->
            size = letters[letter]?.run {
                val letterBounds = (this.map { it.bounds }).bounds
                Vector2(
                    size.x + letterBounds.width + 3,
                    max(letterBounds.height, size.y)
                )
            } ?: Vector2(size.x + 10.0, size.y)
        }

        lines.add(Pair(txt, pos - align * size))
    }

    @Suppress("unused")
    fun hasGlyph(letter: Char) = letters.containsKey(letter)

    override fun setup(program: Program) {
        svg.findShapes().filter { it.id!!.length <= 4 }.forEach {
            val id = it.id!!
            val idChar = if (id.length == 1) id.last() else id.substring(1).toInt().toChar()
            bounds[idChar] = it.bounds
        }
        svg.findShapes().filter { it.id!!.length > 4 }.forEach { curve ->
            for (b in bounds) {
                if (b.value.contains(curve.shape.contours.first().segments.first().start)) {
                    if (!letters.containsKey(b.key)) {
                        letters[b.key] = mutableListOf()
                    }
                    letters[b.key]!!.add(curve.shape.outline.transform(transform {
                        translate(-b.value.corner)
                    }))
                    break
                }
            }
        }
    }

    fun drawToSVG(svg: CompositionDrawer) {
        for ((word, pos) in lines) {
            svg.isolated {
                translate(pos)
                scale(scale)
                word.forEach { letter ->
                    letters[letter]?.run {
                        val letterBounds = (this.map { it.bounds }).bounds
                        translate(-letterBounds.x, 0.0)
                        contours(this)
                        translate(letterBounds.x + letterBounds.width + 3, 0.0)
                    } ?: translate(10.0, 0.0)
                }
            }
        }
    }

    fun draw(drawer: Drawer) {
        for ((word, pos) in lines) {
            drawer.isolated {
                translate(pos)
                scale(scale)
                lineJoin = LineJoin.BEVEL
                word.forEach { letter ->
                    letters[letter]?.run {
                        val letterBounds = (this.map { it.bounds }).bounds
                        translate(-letterBounds.x, 0.0)
                        contours(this)
                        translate(letterBounds.x + letterBounds.width + 3.0, 0.0)
                    } ?: translate(10.0, 0.0)
                }
            }
        }
    }

    override fun afterDraw(drawer: Drawer, program: Program) {
    }
}
