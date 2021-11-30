import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.extra.color.spaces.ColorHSLUVa
import org.openrndr.extra.noise.Random
import org.openrndr.math.Polar
import org.openrndr.poissonfill.PoissonFill
import kotlin.math.sin

/**
 *  This is a template for a live program.
 *
 *  It uses oliveProgram {} instead of program {}. All code inside the
 *  oliveProgram {} can be changed while the program is running.
 */

fun main() = application {
    configure {
        width = 800
        height = 800
    }
    program {
        val dry = renderTarget(width, height) {
            colorBuffer(type = ColorType.FLOAT32)
        }
        val wet = colorBuffer(width, height)

        val fx = PoissonFill()
        val c = drawer.bounds.center
        extend {
            Random.seed = "3"

            drawer.clear(ColorRGBa.BLACK)

            drawer.isolatedWithTarget(dry) {
                clear(ColorRGBa.TRANSPARENT)
                repeat(12) { x ->
                    drawer.strokeWeight = 4.0
                    drawer.stroke = ColorHSLUVa(
                        (Random.int0(3) * 190.0) % 360.0,
                        Random.double0(0.5),
                        0.5 + 0.5 * sin(x + seconds)).toRGBa()
                    val pos =
                        c + Polar(x * 30 + seconds *
                                Random.double(-10.0, 10.0), 300.0).cartesian
                    List(3) {
                        Random.int(-3, 3) * 20.0
                    }.forEachIndexed { i, s ->
                        lineSegment(
                            pos, pos + Polar(seconds * s, 300.0 / (1 + i))
                                .cartesian
                        )
                    }
                }
            }
            fx.apply(dry.colorBuffer(0), wet)
            drawer.shadeStyle = shadeStyle {
                fragmentTransform = """
                    float b = x_fill.r + x_fill.g + x_fill.b;
                    x_fill.rgb *= 0.95 + 0.05 * sin(b * 10.0);
                    """.trimIndent()
            }
            drawer.image(wet)
        }
    }
}
