package apps5

import com.soywiz.korma.random.randomWithWeights
import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.renderTarget
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.Random
import org.openrndr.extra.noise.random
import org.openrndr.extra.palette.PaletteStudio
import org.openrndr.extra.shadestyles.RadialGradient
import org.openrndr.math.Vector2
import kotlin.system.exitProcess

/**
 * Simple program creating a grid of circles shaded to look like 3D,
 * even if they are 2D.
 */

fun main() = application {
    configure {
        width = 800
        height = 800
    }

    program {
        val layer = renderTarget(width, height) {
            colorBuffer()
        }
        val palettes = PaletteStudio()

        fun makeIt() {
            palettes.randomPalette()
            palettes.randomize()
            val colorWeights = List(palettes.colors.size) { if(it > 0) 1.0
                else 20.0 }
            val sizes = listOf(10.0, 13.0, 25.0, 35.0)
            val sizeWeight = listOf(1.0, 2.0, 4.0, 8.0).shuffled()
            drawer.isolatedWithTarget(layer) {
                clear(palettes.background)
                stroke = null
                repeat(8) { x ->
                    repeat(8) { y ->
                        val color =
                            palettes.colors.randomWithWeights(colorWeights)
                                .shade(random(0.8, 1.05))
                        val gradient1 = RadialGradient(
                            color,
                            color.shade(random(0.4, 0.6))
                        ).also {
                            it.exponent = 4.0
                            it.offset = Vector2(0.1, 0.1)
                        }

                        val gradient2 = RadialGradient(
                            color.shade(random(1.0, 1.2)),
                            color.shade(random(0.25, 0.35))
                        ).also {
                            it.exponent = 6.0
                            it.offset = Vector2(-0.1, -0.1)
                        }
                        val pos = bounds.offsetEdges(-150.0).position(
                            x / 7.0, y / 7.0
                        )
                        if(Random.bool(0.95)) {
                            shadeStyle = gradient1
                            val r = sizes.randomWithWeights(sizeWeight)
                            circle(pos, r)
                            shadeStyle = gradient2
                            circle(pos, r / 2)
                        }
                    }
                }
            }
        }

        makeIt()

        extend(Screenshots())
        extend {
            drawer.clear(ColorRGBa.WHITE.shade(0.4))
            drawer.image(layer.colorBuffer(0))
        }
        keyboard.keyUp.listen {
            if (it.key == KEY_ESCAPE) {
                exitProcess(0)
            }
            if (it.name == "n") {
                makeIt()
            }
        }
    }
}