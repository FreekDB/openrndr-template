import color.ColorProviderImage
import editablecurve.contains
import editablecurve.longest
import editablecurve.longestOrientation
import editablecurve.split
import org.openrndr.KEY_ENTER
import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.draw.*
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.fx.blur.GaussianBloom
import org.openrndr.extra.gui.GUI
import org.openrndr.extra.noise.Random
import org.openrndr.extra.parameters.BooleanParameter
import org.openrndr.extra.parameters.Description
import org.openrndr.extra.parameters.DoubleParameter
import org.openrndr.extra.shadestyles.linearGradient
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import org.openrndr.shape.Segment
import org.openrndr.shape.ShapeContour
import shadestyles.perpendicularGradient
import kotlin.math.atan2
import kotlin.system.exitProcess

/**
 * Basic template
 * - [] Fix no intersection but collision
 * - [] Keep color when splitting
 * - [] Sometimes gradient rotated 180° ?
 * - [] Apply colors via shader?
 * - [] Enable color wheel rotation
 */

fun main() = application {
    configure {
        width = 1500
        height = 800
    }

    program {
        val font = loadFont("data/fonts/IBMPlexMono-Regular.ttf", 24.0)
        val curves = mutableListOf<ShapeContour>()
        val colors = ColorProviderImage()
        val src = renderTarget(width, height) {
            colorBuffer(ColorFormat.RGBa, ColorType.FLOAT32)
            depthBuffer()
        }
        val target = colorBuffer(width, height, 1.0, ColorFormat.RGBa, ColorType.FLOAT32)
        val bloom = GaussianBloom()
        bloom.sigma = 0.6
        bloom.window = 5
        bloom.gain = 0.6

        curves.add(Rectangle(0.0, 0.0, width * 1.0, height * 1.0).contour)

        fun doSplit(pos: Vector2) {
            val off = Polar(Random.int0(3) * 120.0, 3000.0).cartesian
            val curve = curves.first { it.contains(pos) }
            val knife = Segment(pos + off, pos - off)
            val parts = curve.split(knife)
            curves.remove(curve)
            curves.add(parts.first)
            curves.add(parts.second)
        }

        val params = @Description("parameters") object {
            @BooleanParameter("bloom")
            var bloomEnabled = false
            @DoubleParameter("x value", 0.0, 640.0)
            var x = 0.5
        }

        extend(GUI()) {
            add(bloom)
            add(params)
        }
        extend(Screenshots())
        extend {
            drawer.isolatedWithTarget(src) {
                with(drawer) {
                    stroke = null
                    fontMap = font
                }

                Random.resetState()
                curves.forEach {
                    val rgb = colors.getColor(Random.double0(360.0))
                    val longest = it.longest()
                    val dir = longest.direction()
                    val orientation = Math.toDegrees(atan2(dir.y, dir.x))
                    drawer.shadeStyle = perpendicularGradient(
                        rgb.shade(1.2), rgb.shade(0.3),
                        rotation = orientation + 90.0,
                        offset = longest.start,
                        exponent = 0.1
                    )
                    drawer.contour(it)
                }
            }
            if (params.bloomEnabled) {
                bloom.apply(src.colorBuffer(0), target)
                drawer.image(target)
            } else {
                drawer.image(src.colorBuffer(0))
            }
        }

        mouse.buttonDown.listen {
            if(!it.propagationCancelled) {
                doSplit(it.position)
            }
        }
        keyboard.keyDown.listen {
            when (it.key) {
                KEY_ESCAPE -> exitProcess(0)
                KEY_ENTER -> colors.reset()
            }
        }
    }
}
