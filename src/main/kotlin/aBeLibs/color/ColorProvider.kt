package aBeLibs.color

import org.openrndr.color.ColorRGBa

interface ColorProvider {
    var offset : Double
    fun getColor(i: Double): ColorRGBa
    fun reset()
}
