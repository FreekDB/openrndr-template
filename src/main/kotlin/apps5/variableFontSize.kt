package apps5

import org.openrndr.application
import org.openrndr.draw.loadFont
import org.openrndr.math.Vector2

/**
 * Two approaches to changing font sizes
 * https://openrndr.discourse.group/t/changing-font-size-on-the-go/318
 */

fun main() = application {
    program {
        // Discrete font sizes
        val fonts = List(10) {
            loadFont(
                "data/fonts/SourceCodePro-Regular.ttf",
                4.0 + 5 * it * it,
                "0123456789".toSet()
            )
        }

        // High resolution font map to scale down
        val font = loadFont(
            "data/fonts/SourceCodePro-Regular.ttf",
            600.0,
            "zom".toSet()
        )

        extend {
            drawer.fontMap = fonts.random()
            drawer.text(frameCount.toString(), 50.0, height - 50.0)

            drawer.fontMap = font
            drawer.translate(50.0, height / 2.0)
            drawer.scale(0.01 + (frameCount * 0.001))
            drawer.text("zoom", Vector2.ZERO)
        }
    }
}