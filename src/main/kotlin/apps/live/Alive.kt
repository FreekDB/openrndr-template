package apps.live

import org.openrndr.application
import org.openrndr.color.rgb
import org.openrndr.color.rgba
import org.openrndr.extra.olive.oliveProgram
import kotlin.math.cos

fun main() {
    application {
        configure {
            width = 900
            height = 900
        }
        oliveProgram {
            extend {
                drawer.clear(rgb(0.1, 0.5, 0.3))
                drawer.stroke = null
                for (i in 0 until 100) {
                    drawer.fill = rgba(1.0, 1.0, 1.0, i / 100.0)
                    drawer.circle(
                        width / 2.0 + cos(seconds + i) * 320.0,
                        80.0+ i * 7.2,
                        cos(i + seconds * 0.5) * 20.0 + 20.0
                    )
                }
            }
        }
    }
}