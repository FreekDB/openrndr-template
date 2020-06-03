package apps.p5

import org.openrndr.KEY_ESCAPE
import org.openrndr.PresentationMode
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.color.rgb
import org.openrndr.extra.shadestyles.linearGradient
import kotlin.system.exitProcess

fun main() = application {
    configure {
        width = 640
        height = 360
    }
    program {
        // Define colors
        val b1 = ColorRGBa.WHITE
        val b2 = ColorRGBa.BLACK
        val c1 = rgb(0.8, 0.4, 0.0)
        val c2 = rgb(0.0, 0.4, 0.6)

        window.presentationMode = PresentationMode.MANUAL
        window.requestDraw()

        extend {
            drawer.stroke = null
            // Background
            drawer.shadeStyle = linearGradient(b1, b2, rotation = -90.0)
            drawer.rectangle(0.0, 0.0, width / 2.0, height.toDouble());
            drawer.shadeStyle = linearGradient(b1, b2, rotation = 90.0)
            drawer.rectangle(width / 2.0, 0.0, width / 2.0, height.toDouble());
            // Foreground
            drawer.shadeStyle = linearGradient(c1, c2, rotation = 0.0)
            drawer.rectangle(50.0, 90.0, 540.0, 80.0);
            drawer.shadeStyle = linearGradient(c1, c2, rotation = 90.0)
            drawer.rectangle(50.0, 190.0, 540.0, 80.0);
        }

        keyboard.keyDown.listen {
            if (it.key == KEY_ESCAPE) exitProcess(0)
        }
    }
}
