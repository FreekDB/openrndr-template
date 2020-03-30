import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.compositor.*
import org.openrndr.extra.fx.blend.Add
import org.openrndr.extra.fx.blur.ApproximateGaussianBlur
import org.openrndr.extra.fx.distort.Perturb
import org.openrndr.extra.fx.dither.CMYKHalftone
import org.openrndr.extra.fx.edges.EdgesWork
import org.openrndr.extra.gui.GUI
import org.openrndr.extra.parameters.Description
import org.openrndr.extra.parameters.DoubleParameter
import org.openrndr.extra.parameters.IntParameter
import org.openrndr.extra.shadestyles.radialGradient
import org.openrndr.ffmpeg.ScreenRecorder
import org.openrndr.math.IntVector2
import kotlin.math.PI
import kotlin.math.cos
import kotlin.system.exitProcess

fun main() = application {
    configure {
        width = 600
        height = 600
        hideWindowDecorations = true
        position = IntVector2(10, 10)
    }

    program {
        extend(ScreenRecorder())

        val param = @Description("parameters") object {
            @DoubleParameter("Halftone 1 dot size", 0.5, 5.0)
            var halftone1dotSize = 0.5

            @DoubleParameter("Halftone 2 dot size", 0.5, 5.0)
            var halftone2dotSize = 0.5

            @DoubleParameter("Halftone 1 scale", 1.0, 50.0)
            var halftone1scale = 0.5

            @DoubleParameter("Halftone 2 scale", 1.0, 50.0)
            var halftone2scale = 0.5

            @IntParameter("Edgeswork radius", 1, 10)
            var edgesWorRadius = 2
        }

        extend(GUI()) {
            add(param)
        }

        val composite = compose {
            println("this is only executed once")

            draw {
            }

            layer {
                blend(Add()) {
                    clip = false
                }
                draw {
                    drawer.shadeStyle = radialGradient(
                        ColorRGBa(1.0, 1.0, 0.0),
                        ColorRGBa(0.2, 0.4, 0.6), length = 1.0
                    )
                    drawer.stroke = null
                    drawer.circle(width * (seconds % 1.0), height * 0.6, 175.0)
                    drawer.rectangle(width * 0.1, height * 0.4, width * 0.8, height * ((seconds * 0.5) % 1))
                }
                post(ApproximateGaussianBlur()) {
                    window = 25
                    sigma = 10.00
                }
                post(CMYKHalftone()) {
                    dotSize = param.halftone1dotSize
                    scale = param.halftone1scale
                }
                post(EdgesWork()) {
                    radius = param.edgesWorRadius
                }
            }

            layer {
                blend(Add()) {
                    clip = false
                }
                draw {
                    drawer.shadeStyle = radialGradient(
                        ColorRGBa(1.0, 0.0, 1.0),
                        ColorRGBa(0.0, 2.0, 4.0), length = 1.0
                    )
                    drawer.stroke = null
                    drawer.circle(width * ((0.2 * seconds) % 1.0), height * 0.4, 175.0)
                    drawer.rectangle(width * 0.1, height * 0.4, width * 0.8, height * ((seconds * 0.4) % 1))
                }
                post(Perturb()) {
                    phase = seconds * 0.1
                    decay = 0.168
                    gain = cos(seconds * 0.25 * PI) * 0.1 + 0.1
                }
                post(ApproximateGaussianBlur()) {
                    window = 40
                    sigma = 15.00
                }
                post(CMYKHalftone()) {
                    dotSize = param.halftone2dotSize
                    scale = param.halftone2scale
                }
                post(EdgesWork()) {
                    radius = param.edgesWorRadius
                }
            }
        }

        extend(Screenshots()) {
            key = "s"
        }
        extend {
            drawer.background(ColorRGBa.BLACK)
            composite.draw(drawer)
        }

        keyboard.keyDown.listen {
            if (it.key == KEY_ESCAPE) {
                exitProcess(0)
            }
        }

    }
}
