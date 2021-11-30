package apps5

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.BlendMode
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noclear.NoClear
import org.openrndr.extra.noise.Random
import org.openrndr.extra.noise.uniform
import org.openrndr.extra.shapes.regularPolygon
import org.openrndr.extras.color.presets.CHOCOLATE
import org.openrndr.extras.color.presets.PLUM
import org.openrndr.extras.color.presets.TOMATO

fun main() = application {
    program {
        var radius = 500.0
        extend(Screenshots())
        extend(NoClear())
        extend {
            drawer.fill = listOf(
                ColorRGBa.PLUM, ColorRGBa.CHOCOLATE, ColorRGBa.TOMATO
            ).random()
            drawer.stroke = null
            drawer.drawStyle.blendMode = listOf(
                BlendMode.ADD, BlendMode.SUBTRACT
            ).random()
            if(Random.bool(0.5))
            drawer.contour(
                regularPolygon(
                    5, drawer.bounds.uniform(100.0), radius
                )
            )
            drawer.defaults()
            radius *= 0.99
        }
    }
}
