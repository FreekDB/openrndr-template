package latest

import aBeLibs.geometry.circleish
import aBeLibs.random.rnd
import aBeLibs.svg.Pattern
import aBeLibs.svg.fill
import org.openrndr.KEY_ENTER
import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.Random
import org.openrndr.extra.noise.uniform
import org.openrndr.math.Vector2
import org.openrndr.math.map
import org.openrndr.shape.drawComposition
import org.openrndr.svg.saveToFile
import org.openrndr.utils.namedTimestamp
import java.io.File

fun main() {
    application {
        configure {
            width = 1500
            height = 500
        }
        program {
            Random.seed = System.currentTimeMillis().toString()

            val svg = drawComposition { }

            fun newDesign() {
                svg.clear()

                Pattern.stroke = false

                for (i in 0 until 5) {
                    val pos = Vector2(
                        i.toDouble().map(0.0, 4.0, 200.0, width - 200.0),
                        height * 0.5
                    )
                    val outline = circleish(pos, 100.0).shape
                    svg.fill(
                        outline, when (i) {
                            0 -> Pattern.STRIPES(
                                1.0 rnd 5.5,
                                0.5 rnd 1.0,
                                0.0 rnd 360.0
                            )
                            1 -> Pattern.HAIR(
                                2.0 rnd 10.0,
                                0.0005 rnd 0.005,
                                4.0 rnd 10.0
                            )
                            2 -> Pattern.PERP(
                                2.0 rnd 10.0,
                                4.0 rnd 10.0
                            )
                            3 -> Pattern.DOTS(
                                0.003,
                                3.0, 15.0,
                                5.0, 3.0
                            )
                            else -> Pattern.CIRCLES(
                                0.2 rnd 2.0,
                                Vector2.uniform(-Vector2.ONE) * 50.0,
                                0.1 rnd 0.5
                            )
                        }
                    )

                }
            }

            newDesign()

            extend(Screenshots())
            extend {
                drawer.clear(ColorRGBa.PINK)
                drawer.composition(svg)
            }

            mouse.buttonDown.listen {
                newDesign()
            }

            keyboard.keyDown.listen {
                when (it.key) {
                    KEY_ENTER -> svg.saveToFile(
                        File(
                            program.namedTimestamp("svg", "print")
                        )
                    )
                    KEY_ESCAPE -> application.exit()
                }
            }

        }
    }
}
