package apps.simpleTests

import aBeLibs.geometry.variableWidthContour
import org.openrndr.applicationSynchronous
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.noise.Random
import org.openrndr.math.Vector3
import kotlin.math.PI
import kotlin.math.cos

fun main() {
    applicationSynchronous {
        program {
            extend {
                drawer.clear(ColorRGBa.PINK)
                drawer.stroke = null

                val points = List(30) {
                    val t = seconds * 0.5 + it * 0.05
                    Vector3(
                        width * 0.5 + Random.simplex(11.1, t) * width * 0.5,
                        height * 0.5 + Random.simplex(17.7, t) * height * 0.5,
                        9 + cos(PI + PI * 2 * it / 29.0) * 8
                    )
                }
                val c = variableWidthContour(points)
                drawer.contour(c)
            }
        }
    }
}
