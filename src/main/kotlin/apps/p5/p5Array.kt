package apps.p5

import org.openrndr.application
import org.openrndr.color.rgb
import org.openrndr.extra.noclear.NoClear
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos

/**
 * id: 59e671d3-237d-44b5-a566-ba8527456aa5
 * description: A port of a Processing example
 * https://github.com/processing/processing-docs/blob/master/content/examples/Basics/Arrays/Array/Array.pde
 * tags: #new
 */

fun main() = application {
    configure {
        width = 640
        height = 360
    }

    program {
        val coswave = List(width) { abs(cos(PI * it / width)) }

        extend(extend(NoClear())) {
            backdrop = {
                var y1 = 0.0
                var y2 = height / 3.0
                coswave.forEachIndexed { x, v ->
                    drawer.stroke = rgb(v)
                    drawer.lineSegment(x.toDouble(), y1, x.toDouble(), y2)
                }

                y1 = y2
                y2 = y1 + y1
                coswave.forEachIndexed { x, v ->
                    drawer.stroke = rgb(v / 4)
                    drawer.lineSegment(x.toDouble(), y1, x.toDouble(), y2)
                }

                y1 = y2
                y2 = height * 1.0
                coswave.forEachIndexed { x, v ->
                    drawer.stroke = rgb(1 - v)
                    drawer.lineSegment(x.toDouble(), y1, x.toDouble(), y2)
                }
            }
        }
        extend {
        }
    }
}
