import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extensions.Screenshots
import org.openrndr.math.IntVector2
import org.openrndr.math.Vector2
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.contour
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

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
            Vector2(cos(a), sin(a)) * (200.0 + 80.0 * org.openrndr.extra.noise.Random.simplex(cos(a), sin(a)))
        }

        hairs.add(ShapeContour.fromPoints(points, true))

        extend(Screenshots()) {
            key = "s"
        }
        extend {

            val v1 = Random.nextDouble()
            val v2 = v1 + Random.nextDouble(0.05, 0.15)
            val n1 = hairs[0].normal(v1)
            val n2 = hairs[0].normal(v2)
            val n = (n1 + n2).normalized
            val p1 = hairs[0].position(v1)
            val p2 = hairs[0].position(v2)
            val d = p1.distance(p2)
            val c = contour {
                moveTo(p1)
                curveTo(p1 + n1 * d, p2 + n2 * d, p2)
            }
            // TODO: verify that c doesn't cross any previous curves
            hairs.add(c)

            drawer.background(ColorRGBa.PINK)

            drawer.translate(width * 0.5, height * 0.5)

            drawer.fill = ColorRGBa.WHITE
            drawer.stroke = ColorRGBa(0.0, 0.0, 0.0, 0.5)

            hairs.forEach { drawer.contour(it) }
        }

        keyboard.keyDown.listen {
            if (it.key == KEY_ESCAPE) {
                application.exit()
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
