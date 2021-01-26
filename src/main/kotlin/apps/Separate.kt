package apps

import aBeLibs.geometry.separated
import org.openrndr.KEY_ENTER
import org.openrndr.KEY_ESCAPE
import org.openrndr.KEY_INSERT
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.color.ColorXSVa
import org.openrndr.dialogs.saveFileDialog
import org.openrndr.draw.LineCap
import org.openrndr.draw.LineJoin
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.Random
import org.openrndr.extra.noise.simplex
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.shape.Circle
import org.openrndr.shape.CompositionDrawer
import org.openrndr.shape.ShapeContour
import org.openrndr.svg.writeSVG
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.system.exitProcess

/**
 * Create a set of random positions on a 2D ring.
 * Iterate separating those positions to avoid overlaps.
 *
 * Printed of page 16.
 */

fun main() = application {
    configure {
        width = 800
        height = 800
    }

    // - [x] Change to separate using shape radius, not just shape center

    program {
        var bgcolor = ColorRGBa.PINK
        val dots = mutableListOf<ShapeContour>()

        var positions = (Random.ring2d(180.0, 190.0, 200) as List<Vector2>).map {
            val radius = 1 + 20 * Random.double0().pow(5.0)
            Circle(it, radius)
        }
        for (i in 0..100) {
            positions = positions.separated(10.0)
        }
        positions.forEach { cir ->
            val seed = System.nanoTime().toInt()
            val maxRadius = cir.radius.toInt()
            for (radius in 1..maxRadius) {
                val points = 2 * (2 * PI * radius).toInt()
                dots.add(ShapeContour.fromPoints(List(points) {
                    val a = 2 * PI * it / points
                    cir.center + Polar(Math.toDegrees(a), radius + 1.0 * simplex(seed, cos(a), sin(a))).cartesian
                }, true))
            }
        }

        fun exportSVG() {
            val svg = CompositionDrawer()
            svg.translate(drawer.bounds.center)
            svg.fill = null
            svg.stroke = ColorRGBa.BLACK
            svg.contours(dots)
            saveFileDialog(supportedExtensions = listOf("svg")) {
                it.writeText(writeSVG(svg.composition))
            }
        }

        extend(Screenshots())
        extend {
            drawer.apply {
                clear(bgcolor)
                translate(bounds.center)
                lineJoin = LineJoin.BEVEL
                lineCap = LineCap.BUTT
                stroke = ColorRGBa.BLACK
                fill = null
                contours(dots)
            }
        }

        keyboard.keyDown.listen {
            when (it.key) {
                KEY_ESCAPE -> exitProcess(0)
                KEY_INSERT -> exportSVG()
                KEY_ENTER -> bgcolor = ColorXSVa(Random.double0(360.0), 0.3, 0.95).toRGBa()
            }
        }
    }
}

