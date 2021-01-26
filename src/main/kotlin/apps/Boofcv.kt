package apps

import aBeLibs.geometry.toContours
import org.openrndr.KEY_ENTER
import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.color.rgb
import org.openrndr.draw.*
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.fx.blur.ApproximateGaussianBlur
import org.openrndr.extra.noise.Random
import org.openrndr.extra.noise.uniformRing
import org.openrndr.extra.palette.PaletteStudio
import org.openrndr.extra.shadestyles.LinearGradient
import org.openrndr.extra.shadestyles.RadialGradient
import org.openrndr.math.Vector2
import org.openrndr.math.map
import org.openrndr.panel.controlManager
import org.openrndr.panel.elements.button
import org.openrndr.panel.elements.div
import org.openrndr.panel.style.*
import org.openrndr.poissonfill.PoissonFill
import org.openrndr.shape.Rectangle
import org.openrndr.shape.ShapeContour
import aBeLibs.random.pickWeighted
import kotlin.math.pow
import kotlin.system.exitProcess


/**
 * BoofCV experiment.
 * 1. Generates a circular pattern made out of rectangles rotated in increments of 45°.
 * 2. Blurs that pattern and uses BoofCV to get the contours.
 * 3. Draws the contours with gradients, colors coming from the palette generator.
 * 4. Uses poisson fill to cover the background with a gradient.
 * Includes a simple GUI to control the program.
 */

fun main() = application {
    configure {
        width = 1920
        height = 1080
    }
    program {
        val multisample = renderTarget(width, height, multisample = BufferMultisample.SampleCount(8)) {
            colorBuffer(type = ColorType.FLOAT32)
            depthBuffer()
        }
        val resolved = colorBuffer(width, height, type = ColorType.FLOAT32)
        val poisoned = colorBuffer(width, height, type = ColorType.FLOAT32)
        val bw = renderTarget(width, height) {
            colorBuffer()
        }
        val bwBlurred = colorBuffer(width, height)
        val poisson = PoissonFill()
        val blur = ApproximateGaussianBlur()
        val extContours = mutableListOf<ShapeContour>()
        var colorIndex = listOf<Int>()
        val screenshots = Screenshots()

        // Prepare palette
        val palette = PaletteStudio(
            loadDefault = true,
            sortBy = PaletteStudio.SortBy.DARKEST,
            collection = PaletteStudio.Collections.THREE,
            colorCountConstraint = 0
        )
        palette.randomPalette()

        // Prepare gradients
        val gradientLin = LinearGradient(ColorRGBa.WHITE, ColorRGBa.WHITE.shade(0.5), Vector2.ZERO)
        gradientLin.exponent = 2.0
        val gradientRad = RadialGradient(rgb(0.2), rgb(0.1))
        gradientRad.exponent = 2.5

        fun doit() {
            drawer.isolatedWithTarget(bw) {
                clear(ColorRGBa.BLACK)
                fill = ColorRGBa.WHITE
                stroke = null
                translate(bounds.center)
                val amount = 100
                for (i in 0..amount) {
                    isolated {
                        translate(Vector2.uniformRing(height * 0.2, height * 0.4))
                        rotate(Random.int0(2) * 45.0)
                        val l = Random.double0()
                        val pc = map(0.0, amount * 1.0, 1.0, 0.0, i * 1.0)
                        val sz = height * 0.05 + height * 0.3 * pc.pow(8.0)
                        val rect = Rectangle.fromCenter(Vector2.ZERO, sz * l * l, sz - sz * l * l)
                        rectangle(rect)
                    }
                }

                for (i in 0..2) {
                    blur.window = 10
                    blur.spread = 1.0
                    blur.gain = 1.0
                    blur.sigma = 10.0 - i * 2.0
                    blur.apply(bw.colorBuffer(0), bwBlurred)
                    extContours.addAll(bwBlurred.toContours(0.3 + i * 0.2, false))
                }

                colorIndex = extContours.map {
                    listOf(0, 1, 2, 3, 4).pickWeighted(palette.colors.mapIndexed { i, _ -> i * i + 1.0 })
                }
            }
        }

        doit()

        val gui = controlManager {
            styleSheet(has class_ "horizontal") {
                paddingLeft = 0.px
                paddingTop = 0.px
                display = Display.FLEX
                flexDirection = FlexDirection.Row
            }

            layout {
                div("horizontal") {
                    button(label = "cycle colors") {}.events.clicked.listen {
                        palette.randomize()
                    }
                    button(label = "new palette") {}.events.clicked.listen {
                        palette.randomPalette()
                    }
                    button(label = "clear") {}.events.clicked.listen {
                        Random.seed = System.currentTimeMillis().toString()
                        extContours.clear()
                    }
                    button(label = "add") {}.events.clicked.listen {
                        doit()
                    }
                    button(label = "screenshot") {}.events.clicked.listen {
                        screenshots.trigger()
                    }
                }
            }
        }
        extend(gui)
        extend(palette)
        extend(screenshots)
        extend {
            drawer.isolatedWithTarget(multisample) {
                clear(ColorRGBa.TRANSPARENT)
                //shadeStyle = gradientRad
                //rectangle(bounds)
                extContours.forEachIndexed { i, it ->
                    stroke = null
                    fill = palette.colors[colorIndex[i]]
                    gradientLin.rotation = i * 45.0
                    shadeStyle = gradientLin
                    contour(it)

                    val j = (i + 1) % palette.colors.size
                    fill = palette.colors[colorIndex[j]]
                    gradientLin.rotation = j * 45.0
                    shadeStyle = gradientLin
                    isolated {
                        translate(width * 1.0, 0.0)
                        scale(-1.0, 1.0)
                        contour(it)
                    }
                }
//                fill = null
//                strokeWeight = 3.0
//                stroke = rgb(0.1)
//                rectangle(bounds)
            }
            multisample.colorBuffer(0).resolveTo(resolved)
            poisson.apply(resolved, poisoned)
            drawer.image(poisoned)
        }
        keyboard.keyDown.listen {
            when (it.key) {
                KEY_ESCAPE -> exitProcess(0)
                KEY_ENTER -> palette.randomize()
            }
        }
    }
}
