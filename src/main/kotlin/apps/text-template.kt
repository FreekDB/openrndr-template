package apps

import org.openrndr.KEY_ESCAPE
import org.openrndr.KEY_INSERT
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.dialogs.saveFileDialog
import org.openrndr.draw.LineJoin
import org.openrndr.draw.isolated
import org.openrndr.extensions.Screenshots
import org.openrndr.math.transforms.transform
import org.openrndr.shape.CompositionDrawer
import org.openrndr.shape.Rectangle
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.bounds
import org.openrndr.svg.loadSVG
import org.openrndr.svg.writeSVG
import java.util.*
import kotlin.system.exitProcess

/**
 * id: 33a20597-6457-4c12-8f23-51b55832bab8
 * description: Writing handwritten text
 * tags: #axi #svg
 */

/**
 * - Inspiration: https://duckduckgo.com/?t=ffab&q=patent+drawing&iax=images&ia=images
 * - Inspiration: https://stackoverflow.com/questions/27893042/text-to-phonemes-converter
 * - in inkscape, have labeled rectangles and draw on top of them.
 *   then openrndr scans the file, makes a list of rectangles, looks at their IDs,
 *   finds shapes inside them, stores them on a dictionary, then I can write
 *   using that hand writing without creating a font. Also, the shapes are made out
 *   of lines, better for the axidraw
 *
 * See `Handwritten` class used in page3-Extinct.kt
 */

fun main() = application {
    configure {
        width = 800
        height = 800
    }

    program {
        val svg = loadSVG("data/text-template-3.svg")

        val bounds = mutableMapOf<Char, Rectangle>()
        val letters = mutableMapOf<Char, MutableList<ShapeContour>>()

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

        val text = listOf(
            "I'm baby kogi poke DIY shaman cold-pressed. Palo santo pitchfork trust",
            "fund wayfarers. Humblebrag direct trade pour-over, fanny pack venmo vinyl",
            "succulents roof party gastropub portland mustache thundercats. Fingerstache",
            "tumblr dreamcatcher coloring book, brunch bicycle rights bitters health goth",
            "chia snackwave cloud bread leggings ennui heirloom pickled.",
            "",
            "Knausgaard echo park twee tacos, helvetica coloring book enamel pin man",
            "braid lomo photo booth cronut ennui hot chicken. DIY selfies disrupt",
            "gochujang squid cold-pressed. Yuccie microdosing freegan sartorial.",
            "Microdosing narwhal fanny pack dreamcatcher ramps godard."
        )

        fun exportSVG() {
            val svg = CompositionDrawer()
            svg.run {
                fill = null
                stroke = ColorRGBa.BLACK
                text.forEach { line ->
                    isolated {
                        line.trimIndent().uppercase(Locale.getDefault()).forEach {
                            letters[it]?.run {
                                val letterBounds = this.map { c -> c.bounds }.bounds
                                translate(-letterBounds.x, 0.0)
                                contours(this)
                                translate(letterBounds.x + letterBounds.width + 3.0, 0.0)
                            } ?: translate(10.0, 0.0)
                        }
                    }
                    translate(0.0, 20.0)
                }
            }
            saveFileDialog(supportedExtensions = listOf("svg")) {
                it.writeText(writeSVG(svg.composition))
            }
        }

        extend(Screenshots())
        extend {
            drawer.run {
                clear(ColorRGBa.WHITE)
                fill = null
                stroke = ColorRGBa.BLACK
                lineJoin = LineJoin.BEVEL
                translate(100.0, 100.0)
                text.forEach { line ->
                    isolated {
                        line.trimIndent().uppercase(Locale.getDefault()).forEach {
                            letters[it]?.run {
                                val letterBounds =
                                    this.map { c -> c.bounds }.bounds
                                translate(-letterBounds.x, 0.0)
                                contours(this)
                                translate(letterBounds.x + letterBounds.width + 3.0, 0.0)
                            } ?: translate(10.0, 0.0)
                        }
                    }
                    translate(0.0, 20.0)
                }
            }
        }

        keyboard.keyDown.listen {
            when (it.key) {
                KEY_ESCAPE -> exitProcess(0)
                KEY_INSERT -> exportSVG()
            }
        }
    }
}
