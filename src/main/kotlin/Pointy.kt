import editablecurve.ensureExtension
import editablecurve.intersects
import geometry.distance
import org.openrndr.KEY_ESCAPE
import org.openrndr.KEY_INSERT
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.dialogs.saveFileDialog
import org.openrndr.draw.isolated
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.Random
import org.openrndr.extra.noise.simplex
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.shape.CompositionDrawer
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.contour
import org.openrndr.svg.writeSVG
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.system.exitProcess

fun main() = application {
    configure {
        width = 15 * 60
        height = 12 * 60
    }

    program {
        var hairs = mutableListOf<ShapeContour>()

        val used = mutableListOf<Pair<Double, Double>>()
        val seed = System.currentTimeMillis()

        val mother = ShapeContour.fromPoints(List(100) {
            val a = 2 * PI * it / 100.0
            Polar(Math.toDegrees(a), 200.0 + 80.0 * simplex(seed.toInt(), cos(a), sin(a))).cartesian
        }, true)

        fun exportSVG() {
            val svg = CompositionDrawer()
            svg.translate(drawer.bounds.center)
            svg.fill = null
            svg.stroke = ColorRGBa.BLACK
            svg.contour(mother)
            svg.contours(hairs)
            saveFileDialog(supportedExtensions = listOf("svg")) {
                it.writeText(writeSVG(svg.composition))
            }
        }

        extend(Screenshots())
        extend {
            val jmp = 0.01 + seconds * 0.02
            var v1 : Double
            var v2 : Double
            if (Random.bool(0.3) || used.isEmpty()) {
                v1 = Random.double0()
                v2 = (v1 + Random.double(0.01, jmp * 3)) % 1.0
            } else {
                val v = Random.pick(used)
                val offset = Random.double(-0.05, 0.05)
                v1 = v.first + offset
                v2 = v.second - offset
            }
            val n1 = mother.normal(v1)
            val n2 = mother.normal(v2)
            val p1 = mother.position(v1)
            val p2 = mother.position(v2)

            val d = p1.distance(p2)
            val side = Random.int0(2) * 2.0 - 1.0
            val c = contour {
                moveTo(p1)
                curveTo(p1 + n1 * d * side, p2 + n2 * d * side, p2)
            }
            val hair = c.sampleLinear(1.0)
            if (hairs.all { it.intersects(hair) == Vector2.INFINITY }) {
                hairs.add(hair)
                used.add(Pair(v1, v2))
            }

            drawer.background(ColorRGBa.PINK)

            drawer.isolated {
                drawer.translate(drawer.bounds.center)
                drawer.fill = null
                drawer.stroke = ColorRGBa(0.0, 0.0, 0.0, 0.5)
                drawer.contour(mother)
                drawer.contours(hairs)
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

