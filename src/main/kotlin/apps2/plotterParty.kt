package apps2

import extensions.Handwritten
import geometry.localDistortion
import geometry.softJitter
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.dialogs.*
import org.openrndr.draw.LineJoin
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.Random
import org.openrndr.math.Vector2
import org.openrndr.shape.CompositionDrawer
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.contour
import org.openrndr.svg.writeSVG
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sign

/**
 * Plotter party
 */

fun main() = application {
    configure {
        width = 210 * 5
        height = 148 * 5
    }
    program {
        Random.seed = System.currentTimeMillis().toString()

        var svg = CompositionDrawer()
        val handwritten = Handwritten().apply {
            scale = 1.5
        }

        val points = listOf(
            Vector2(10.0, 2.0),
            Vector2(5.0, 8.0),
            Vector2(6.0, 7.0),
            Vector2(10.0, 7.0),
            Vector2(1.0, 12.0),
            Vector2(4.0, 4.0),
            Vector2(3.0, 13.0)
        )
        val pointsScreen = points.map { it * Vector2(width / 14.0, height / 14.0) }
        points.forEachIndexed { i, p ->
            handwritten.add("P$i ${p.x}, ${p.y}", pointsScreen[i], Vector2(0.2, 0.5))
        }

        fun populate() {
            val curves = mutableListOf<ShapeContour>()
            val base = MutableList(points.size) {
                val a = it
                val b = Random.int(a + 1, a + pointsScreen.size - 1) % pointsScreen.size
                contour {
                    moveTo(pointsScreen[a])
                    curveTo(
                        drawer.bounds.position(Random.double0(), Random.double0(1.0)),
                        drawer.bounds.position(Random.double0(), Random.double0(1.0)), pointsScreen[b]
                    )
                }.softJitter(15, 0.0, 0.1)
            }

            val distortionData = List(16) {
                val n = it - 7.5
                val dist = sign(n) * (abs(n) + 0.5).pow(2.0)
                Triple(0.1, 0.9, dist)
            }
            base.forEach {
                curves.addAll(it.localDistortion(distortionData, 500))
            }
            svg = CompositionDrawer()
            svg.fill = null
            svg.stroke = ColorRGBa.BLACK
            svg.contours(curves)
            //svg.rectangle(drawer.bounds)
            svg.circles(pointsScreen, 5.0)
            handwritten.drawToSVG(svg)
        }
        populate()

        extend(handwritten)
        extend(Screenshots())
        extend {
            drawer.run {
                clear(ColorRGBa.WHITE)
                fill = null
                lineJoin = LineJoin.BEVEL
                composition(svg.composition)
                circles(pointsScreen, 5.0)
                rectangle(bounds)
                handwritten.draw(this)
            }
        }
        keyboard.keyDown.listen {
            if (it.name == "s") {
                saveFileDialog(supportedExtensions = listOf("vector" to listOf("svg", "SVG"))) { f ->
                    f.writeText(writeSVG(svg.composition))
                }
            }
            if (it.name == "r") {
                populate()
            }
        }
    }
}

