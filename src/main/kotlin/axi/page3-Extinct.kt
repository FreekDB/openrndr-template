package axi

import aBeLibs.extensions.Handwritten
import aBeLibs.geometry.eraseEndsWithCircles
import aBeLibs.geometry.localDistortion
import aBeLibs.geometry.noised
import aBeLibs.geometry.softJitter
import org.openrndr.KEY_ESCAPE
import org.openrndr.KEY_INSERT
import org.openrndr.applicationSynchronous
import org.openrndr.color.ColorRGBa
import org.openrndr.color.rgb
import org.openrndr.dialogs.saveFileDialog
import org.openrndr.draw.LineJoin
import org.openrndr.draw.shadeStyle
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.glslify.preprocessGlslify
import org.openrndr.extra.noise.Random
import org.openrndr.math.Vector2
import org.openrndr.math.map
import org.openrndr.shape.*
import org.openrndr.svg.writeSVG
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sign
import kotlin.system.exitProcess

/**
 * Curves with few points.
 * Create a method for adding localized divergence in the line.
 */

fun main() = applicationSynchronous {

    configure {
        width = 1500
        height = 800
    }

    program {
        val handwritten = Handwritten()
        val lines = mutableListOf<ShapeContour>()
        val circles = mutableListOf<ShapeContour>()

        extend(handwritten)

        // val words = File("/usr/share/cracklib/cracklib-small").readLines()
        // usage: Random.pick(words)

        // https://en.wikipedia.org/wiki/List_of_extinct_plants
        val words = listOf(
            "Trochetiopsis melanoxylon" to 1771,
            "Heliotropium pannifolium" to 1808,
            "Streblorrhiza speciosa" to 1860,
            "Neomacounia nitida" to 1864,
            "Melicope haleakalae" to 1919,
            "Viola cryana" to 1933,
            "Ormosia howii" to 1957,
            "Achyranthes atollensis" to 1964,
            "Guettarda retusa" to 1975,
            "Licania caldasiana" to 1997
        )

        Random.seed = System.currentTimeMillis().toString()

        for (i in 0 until 10) {
            val xPos = i.toDouble().map(0.0, 9.0, width * 0.1, width * 0.9)
            val distToCenter = abs(xPos - width * 0.5) / (width * 0.5) // center 0.0, sides 1.0
            val radius = 1.0 / (1.0 + distToCenter) // center 1.0, sides 0.5

            var baseContour = contour {
                moveTo(xPos, height * 0.1 + distToCenter * 150)
                curveTo(
                    xPos + 300.0, height * 0.1,
                    xPos - 300.0, height * 0.9,
                    xPos, height * 0.9
                )
            }

            val circlePair = listOf(
                Circle(baseContour.position(0.0), Random.double(3.1, 4.8)),
                Circle(baseContour.position(1.0), Random.double(3.1, 4.8))
            )

            val (name, year) = words[i]
            handwritten.add(year.toString(), circlePair[0].center, Vector2(0.5, 3.0))
            name.split(" ").forEachIndexed { i, n ->
                handwritten.add(n, circlePair[1].center + Vector2(20.0, i * 15.0 - 35.0), Vector2(0.8, 0.0))
            }

            baseContour = baseContour.softJitter(5, radius * 0.1, radius * 0.2)
            baseContour = baseContour.eraseEndsWithCircles(circlePair[0], circlePair[1])

            val distCenter = i.toDouble().map(0.0, 9.0, 0.35, 0.65)
            val distortionData = List(16) {
                val n = it - 7.5
                val dist = sign(n) * (abs(n) + 0.5).pow(1.6)
                Triple(
                    distCenter - 0.12 - 0.002 * abs(dist),
                    distCenter + 0.12 + 0.002 * abs(dist),
                    dist
                )
            }
            val copies = baseContour.localDistortion(distortionData, 300)

            lines.addAll(copies)
            circlePair.forEach { circle ->
                Random.double(0.4, 0.6).let { cut ->
                    val c1 = circle.contour.sub(cut, cut + 0.9)
                    val c2 = contour {
                        for (s in c1.segments) {
                            val start = s.start.noised()
                            val end = s.end.noised()
                            val ctrl = s.control.map { it.noised() }
                            segment(Segment(start, ctrl.toTypedArray(), end))
                        }
                    }
                    circles.add(c2)
                }
            }

            // TODO: Create curve.dashed()
        }

        fun exportSVG() {
            val svg = CompositionDrawer()
            svg.fill = null
            svg.stroke = ColorRGBa.BLACK
            svg.contours(lines)
            svg.contours(circles)
            handwritten.drawToSVG(svg)
            saveFileDialog(supportedExtensions = listOf("svg")) {
                it.writeText(writeSVG(svg.composition))
            }
        }

        val glslSimplex = preprocessGlslify(
            "#pragma glslify: simplex = require(glsl-noise/simplex/2d)"
        )

        extend(Screenshots())
        extend {
            drawer.run {
                clear(rgb(0.933333, 0.854902, 0.729412))
                lineJoin = LineJoin.BEVEL

                shadeStyle = shadeStyle {
                    fragmentPreamble = glslSimplex
                    fragmentTransform = """
                        x_stroke.rgb += 0.1 + 0.1 * simplex(gl_FragCoord.xy * 0.012);
                    """.trimMargin()
                }
                stroke = rgb(0.568627, 0.486275, 0.372549)
                fill = null

                contours(lines)
                contours(circles)

                shadeStyle = null
                handwritten.draw(this)
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
