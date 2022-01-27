package apps5

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.renderTarget
import org.openrndr.extensions.Screenshots
import java.io.File
import kotlin.system.exitProcess

/**
 * Example for the forum that saves an image to disk and quits
 */

fun main() = application {
    configure {
        width = 64
        height = 64
        hideWindowDecorations = true
    }

    program {
        val canvas = renderTarget(4000, 4000) {
            colorBuffer()
        }
        drawer.isolatedWithTarget(canvas) {
            clear(ColorRGBa.WHITE)
            stroke = null
            fill = ColorRGBa.PINK
            ortho(canvas)
            circle(canvas.width / 2.0, canvas.height / 2.0, 1000.0)
        }
        canvas.colorBuffer(0).saveToFile(File("/tmp/result.png"), async = false)
        exitProcess(0)
    }
}