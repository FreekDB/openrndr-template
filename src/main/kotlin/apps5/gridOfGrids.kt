package apps5

import aBeLibs.fx.WideColorCorrection
import com.soywiz.korma.random.randomWithWeights
import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.createEquivalent
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.renderTarget
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.fx.blend.Add
import org.openrndr.extra.fx.blur.ApproximateGaussianBlur
import org.openrndr.extra.noise.Random
import org.openrndr.extra.noise.random
import org.openrndr.extra.palette.PaletteStudio
import org.openrndr.extra.shadestyles.LinearGradient
import org.openrndr.extra.shapes.grid
import org.openrndr.shape.Rectangle
import kotlin.system.exitProcess

/**
 * Simple program creating a grid of circles shaded to look like 3D,
 * even if they are 2D.
 */

fun main() = application {
    configure {
        width = 1200
        height = 1200
    }

    program {
        // Textures
        val drawLayer = renderTarget(width, height) {
            colorBuffer()
        }
        val bufColorCorrected = drawLayer.colorBuffer(0).createEquivalent()
        val bufBlurred = bufColorCorrected.createEquivalent()
        val bufGradientsBlurred = bufColorCorrected.createEquivalent()

        // Effects
        val colorCorrection = WideColorCorrection().apply {
            brightness = -0.5
            contrast = 1.0
            saturation = -0.3
            gamma = 1.5
            opacity = 1.0
        }
        val blur = ApproximateGaussianBlur().apply {
            window = 25
            spread = 2.0
            gain = 1.0
            sigma = 10.0
        }
        val add = Add()

        val palettes = PaletteStudio(
            sortBy = PaletteStudio.SortBy.DARKEST,
            collection = PaletteStudio.Collections.THREE
        )
        val colorWeights = List(palettes.colors.size) {
            1.0 / (1.0 + it * it)
        }

        fun grid(rect: Rectangle) {
            if (rect.width > 20 && rect.height > 20 && Random.bool(0.8)) {
                rect.grid(
                    Random.int(1, 4),
                    Random.int(1, 4),
                    0.0, 0.0, 5.0, 5.0
                ).flatten().forEach {
                    grid(it)
                }
            } else {
                val color =
                    palettes.colors.randomWithWeights(colorWeights)
                        .shade(random(0.8, 1.05))

                drawer.shadeStyle = LinearGradient(
                    color,
                    color.shade(random(0.7, 0.9))
                ).also {
                    it.exponent = 4.0
                    it.rotation = 90.0
                }
                drawer.rectangle(rect)
            }
        }

        fun makeIt() {
            palettes.randomPalette()
            //palettes.randomize()
            drawer.isolatedWithTarget(drawLayer) {
                clear(palettes.background)
                stroke = null
                grid(drawer.bounds.offsetEdges(-10.0))
            }
            colorCorrection.apply(
                drawLayer.colorBuffer(0),
                bufColorCorrected
            )
            blur.apply(bufColorCorrected, bufBlurred)
            add.apply(
                arrayOf(drawLayer.colorBuffer(0), bufBlurred),
                bufGradientsBlurred
            )

        }

        makeIt()

        extend(Screenshots())
        extend {
            drawer.clear(ColorRGBa.WHITE.shade(0.4))
            drawer.image(bufGradientsBlurred)
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