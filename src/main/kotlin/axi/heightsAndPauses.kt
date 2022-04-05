package axi

import aBeLibs.svg.saveToInkscapeFile
import aBeLibs.svg.setInkscapeLayer
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.drawComposition
import org.openrndr.math.Vector2
import org.openrndr.math.map
import java.io.File
    
/**
 * id: f3c142a7-3022-4719-b5a8-7f9835c83d22
 * description: New sketch
 * tags: #new
 */    

fun main() = application {
    program {
        val comp = drawComposition {
            translate(drawer.bounds.center)
            stroke = ColorRGBa.BLACK

            val times = 5
            val start = 60.0
            val end = 95.0

            repeat(times) {
                val h = it.toDouble().map(
                    0.0, times - 1.0, start, end
                ).toInt()
                val layerName = if (it > 0)
                    "!+D4000+H$h layer$it"
                else
                    "+H$h layer$it"

                group {
                    circle(Vector2.ZERO, 30.0 + it * 5)
                }.setInkscapeLayer(layerName)
            }
        }
        comp.saveToInkscapeFile(File("/tmp/layers.svg"))
        application.exit()
    }
}
