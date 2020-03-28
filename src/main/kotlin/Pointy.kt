import editablecurve.intersects
import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolated
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.Random.simplex
import org.openrndr.extra.palette.PaletteStudio
import org.openrndr.extra.shapes.regularStar
import org.openrndr.math.IntVector2
import org.openrndr.math.Vector2
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.contour
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlin.system.exitProcess

fun main() = application {
    configure {
        width = 600
        height = 600
        hideWindowDecorations = true
        position = IntVector2(10, 10)
    }

    program {
        var hairs = mutableListOf<ShapeContour>()

        val points = List(100) {
            val a = 2 * PI * it / 100.0
            Vector2(cos(a), sin(a)) * (200.0 + 80.0 * simplex(cos(a), sin(a)))
        }

        var star = regularStar(6, 50.0,
            70.0, Vector2(0.0), 15.0)


        var rp = PaletteStudio()
        rp.randomPalette()
        rp.randomPalette()

        val mother = ShapeContour.fromPoints(points, true)

        extend(Screenshots()) {
            key = "s"
        }
        extend {

            val v1 = Random.nextDouble()
            val v2 = (v1 + Random.nextDouble(0.05, 0.15)) % 1.0
            val n1 = mother.normal(v1)
            val n2 = mother.normal(v2)
            val p1 = mother.position(v1)
            val p2 = mother.position(v2)
            val d = p1.distance(p2)
            val c = contour {
                moveTo(p1)
                curveTo(p1 + n1 * d, p2 + n2 * d, p2)
            }
            val hair = c.sampleLinear(5.0)
            if(hairs.all { it.intersects(hair) == Vector2.INFINITY }) {
                hairs.add(hair)
            }

            drawer.background(ColorRGBa.PINK)

            drawer.isolated {
                drawer.translate(width * 0.5, height * 0.5)
                drawer.fill = ColorRGBa.WHITE
                drawer.stroke = ColorRGBa(0.0, 0.0, 0.0, 0.5)
                drawer.contour(mother)
                hairs.forEach { drawer.contour(it) }
                drawer.contour(star)
            }

            rp.palette.colors.forEachIndexed {
                x, c ->
                drawer.fill = c
                drawer.rectangle(x * 15.0 + 10.0, height - 50.0, 10.0, 40.0)
            }
        }

        keyboard.keyDown.listen {
            if (it.key == KEY_ESCAPE) {
                exitProcess(0)
            }
        }

    }
}

fun mix(a: Vector2, b: Vector2, x: Double): Vector2 {
    return a * (1.0 - x) + b * x
}

private fun Vector2.distance(other: Vector2): Double {
    return (this - other).length
}
