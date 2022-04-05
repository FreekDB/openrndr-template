package tutorials

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.color.rgb
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.loadFont
import org.openrndr.draw.renderTarget
    
/**
 * id: 4dcdc4de-169c-4fa2-b4af-e53c26f1d287
 * description: New sketch
 * tags: #new
 */    

fun main() = application {
    configure {
        width = 640
        height = 200
    }
    program {
        val font = loadFont("data/fonts/SourceCodePro-Regular.ttf", 96.0)

        val rt = renderTarget(200, 200) {
            colorBuffer()
        }

        drawer.isolatedWithTarget(rt) {
            ortho(rt)
            clear(ColorRGBa.TRANSPARENT)
            fill = ColorRGBa.BLACK
            scale(0.5)
            fontMap = font
            text("test isolatedWithTarget text", 50.0, rt.height * 1.0)
        }

        extend {
            drawer.clear(ColorRGBa.WHITE)
            drawer.image(rt.colorBuffer(0))
        }
    }
}
