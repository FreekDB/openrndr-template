package apps.p5

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.noise.Random
import org.openrndr.shape.Circle
import kotlin.math.PI
import kotlin.math.sin

private data class SineWave(val freq: Double, val shift: Double, val amp: Double) {
    fun value(t: Double, x: Double) = amp * sin(t * freq + shift * x)
}

fun main() = application {
    program {
        val numPoints = 80
        val sineWaves = List(5) {
            SineWave(-5 rnd 5, 0 rnd PI * 2, 10 rnd 30)
        }
        extend {
            drawer.clear(ColorRGBa.BLACK)
            drawer.fill = ColorRGBa.WHITE.opacify(0.3)
            drawer.stroke = null
            drawer.circles(List(numPoints) { num ->
                val x = width * num / (numPoints - 1.0)
                val y = height * 0.5 + sineWaves.sumOf {
                    it.value(seconds, x * 0.01)
                }
                Circle(x, y, 10.0)
            })
        }
    }
}

private infix fun Number.rnd(max: Number) =
    Random.double(this.toDouble(), max.toDouble())
