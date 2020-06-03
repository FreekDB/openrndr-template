package p5

import org.openrndr.KEY_ESCAPE
import org.openrndr.PresentationMode
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.color.rgb
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.shadestyles.linearGradient
import kotlin.system.exitProcess

fun main() = application {
    configure {
        width = 800
        height = 800
    }
    program {
        val c1 = rgb(0.8, 0.4, 0.0)
        val c2 = rgb(0.0, 0.4, 0.6)

        extend {
            drawer.stroke = null
            drawer.shadeStyle = linearGradient(c1, c2, rotation = 45.0)
            val parts = 20
            val sz = width.toDouble() / parts
            for (x in 0 until parts) {
                for (y in 0 until parts) {
                    drawer.rectangle(x * sz, y * sz, sz, sz);
                }
            }
        }
    }
}
