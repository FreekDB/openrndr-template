package apps2

import org.openrndr.KEY_ESCAPE
import org.openrndr.KEY_INSERT
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.dialogs.saveFileDialog
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.uniformRing
import org.openrndr.math.Vector2
import org.openrndr.shape.CompositionDrawer
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.contour
import org.openrndr.svg.writeSVG
import kotlin.system.exitProcess

private val Vector2.rnd: Vector2
    get() {
        return this + Vector2.uniformRing(outerRadius = 1.5)
    }

var wordCursor = Vector2.ZERO

/**
 * Writing text
 * - Algorithmic text generation
 * - Inspired by P5 wordsToShapes from 2013/06
 *   https://github.com/hamoid/Fun-Programming/tree/master/processing/ideas/2013/06/wordsToShapes
 * -Dopenrndr.gl3.debug=true
 */

fun main() = application {
    configure {
        width = 800
        height = 800
    }

    fun MutableList<ShapeContour>.add(p0: Vector2, c0: Vector2, c1: Vector2, p1: Vector2) {
        add(contour {
            moveTo(p0)
            curveTo(c0.rnd, c1.rnd, p1)
        })
    }

    fun word(word: String): List<ShapeContour> {
        val k = 10.0
        // this defines a grid of 3 columns (Left, Middle, Right) and 5 rows
        val l = List(5) { Vector2(-0.5, -1.0 + it * 0.5) * k + wordCursor }
        val m = List(5) { Vector2(0.0, -1.0 + it * 0.5) * k + wordCursor }
        val r = List(5) { Vector2(0.5, -1.0 + it * 0.5) * k + wordCursor }

        val result = mutableListOf<ShapeContour>()

        word.forEach { char ->
            result.run {
                when (char) {
                    'a' -> add(l[0], l[2], l[2], l[4]) // L |
                    'b' -> add(r[0], r[2], r[2], r[4]) // R |
                    'c' -> add(l[0], m[0], m[0], r[0]) // H -
                    'd' -> add(l[2], m[2], m[2], r[2]) // M -
                    'e' -> add(m[0], m[1], m[1], m[2]) // '

                    'f' -> add(m[2], m[3], m[3], m[4]) // ,
                    'g' -> add(l[2], m[1], m[1], r[0]) // H /
                    'h' -> add(l[0], m[1], m[1], r[2]) // H \
                    'i' -> add(l[4], m[3], m[3], r[2]) // L /
                    'j' -> add(l[2], m[3], m[3], r[4]) // L \

                    'k' -> add(l[4], m[4], m[4], r[4]) // L -
                    'l' -> add(l[1], m[1], m[1], r[1]) // HM -
                    'm' -> add(l[3], m[3], m[3], r[3]) // ML -

                    'n' -> add(l[1], l[0], l[0], m[0]) // TL o North
                    'o' -> add(m[0], r[0], r[0], r[1]) // TR o North
                    'p' -> add(r[1], r[2], r[2], m[2]) // BR o North
                    'q' -> add(m[2], l[2], l[2], l[1]) // BL o North

                    'r' -> add(l[3], l[2], l[2], m[2]) // TL o South
                    's' -> add(m[2], r[2], r[2], r[3]) // TR o South
                    't' -> add(r[3], r[4], r[4], m[4]) // BR o South
                    'u' -> add(m[4], l[4], l[4], l[3]) // BL o South

                    'v' -> add(l[2], l[1], l[1], m[1]) // TL o Mid
                    'w' -> add(m[1], r[1], r[1], r[2]) // TR o Mid
                    'x' -> add(r[2], r[3], r[3], m[3]) // BR o Mid
                    'y' -> add(m[3], l[3], l[3], l[2]) // BL o Mid

                    'z' -> add(l[4], m[2], m[2], r[0])
                    ',' -> add(l[4], m[4], r[3], r[2])
                    '.' -> add(l[4], l[2], r[2], r[4])
                    '?' -> add(l[0], r[0], r[2], l[2])
                }
            }
        }

        wordCursor += Vector2(15.0, 0.0)
        if (wordCursor.x > 350) {
            wordCursor = Vector2(200.0, wordCursor.y + 30)
        }

        return result
    }

    wordCursor = Vector2(200.0, 200.0)

    val words = mutableListOf<List<ShapeContour>>()
    """have most of the important
      | things been said already ?
      | that will depend on how long
      | we survive as a species .
      | if most of the important
      | things are yet to come that
      | probably means we have a long
      | future ahead of us .""".trimMargin()
        .split(" ")
        .forEach {
            words.add(word(it))
        }

    program {

        fun exportSVG() {
            val svg = CompositionDrawer()
            svg.run {
                fill = null
                stroke = ColorRGBa.BLACK
                words.forEach {
                    contours(it)
                }
            }
            saveFileDialog(supportedExtensions = listOf("svg")) {
                it.writeText(writeSVG(svg.composition))
            }
        }

        extend(Screenshots())
        extend {
            drawer.run {
                background(ColorRGBa.WHITE)
                fill = null
                stroke = ColorRGBa.BLACK
                words.forEach {
                    contours(it)
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
