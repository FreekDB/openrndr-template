import org.openrndr.KEY_ENTER
import org.openrndr.KEY_ESCAPE
import org.openrndr.KEY_INSERT
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.color.ColorXSVa
import org.openrndr.dialogs.saveFileDialog
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.Random
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.math.map
import org.openrndr.shape.CompositionDrawer
import org.openrndr.shape.ShapeContour
import org.openrndr.svg.writeSVG
import kotlin.math.pow
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

    // TODO: change order of: separate centers -> generate shapes
    // to: generate shapes -> separate shapes (using center + radius)

    program {
        var bgcolor = ColorRGBa.PINK
        var dots = mutableListOf<ShapeContour>()

        var positions = Random.ring2d(150.0, 200.0, 128) as List<Vector2>
        for (i in 0..70) {
            positions = positions.separate(50.0)
        }
        positions.forEach { center ->
            var a = 0.0
            val powFactor = Random.simplex(center.x * 0.01, center.y * 0.01).map(-1.0, 1.0, 0.7, 3.0)
            //val radFactor = Random.simplex(center.y * 0.01, center.x * 0.01).map(-1.0, 1.0, 0.01, 0.25)
            var radius = 0.0
            dots.add(ShapeContour.fromPoints(List(Random.int(12, 100)) {
                // irregular
                //a += it.toDouble().pow(powFactor)

                // smooth
                a += 30.0
                radius += 0.1 / (1.0 + it * 0.01)
                center + Polar(a, radius).cartesian
            }, false))
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
                background(bgcolor)
                translate(bounds.center)
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

fun List<Vector2>.separate(separation: Double): List<Vector2> {
    return this.map { me ->
        var sum = Vector2.ZERO
        var count = 0
        this.forEach { other ->
            val d = me.distanceTo(other)
            if (d > 0.0 && d < separation) {
                var force = (me - other).normalized
                force /= d
                sum += force
                count++;
            }
        }
        if (count > 0) {
            sum = sum.normalized
        }
        me + sum
    }

}
