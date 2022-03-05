package apps.simpleTests

import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolated
import org.openrndr.draw.loadFont
import org.openrndr.draw.writer
import org.openrndr.extensions.Screenshots
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.shape.SegmentJoin
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.contour
import org.openrndr.shape.offset
import kotlin.system.exitProcess

/**
 * id: 4ed1ae22-6e3a-40ba-afd5-6c87aa603195
 * description: Testing recent changes to ShapeContour and .offset()
 * tags: #new
 */

fun main() = application {
    configure {
        width = 1500
        height = 800
    }

    program {
        val font = loadFont("data/fonts/IBMPlexMono-Regular.ttf", 24.0)
        val curves = mutableListOf<ShapeContour>()
        val itemsPerSet = 7
        var headline = ""

        fun Int.toCartesian(): Vector2 {
            return Polar(this.toDouble(), 50.0).cartesian
        }

        fun populate(type: Int) {
            curves.clear()

            headline = when (type) {
                5 -> "pointy concave shape"
                4 -> "concave shape"
                3 -> "curves, increasing angle"
                2 -> "curves, decreasing angle"
                1 -> "lines, decreasing angle"
                else -> "lines, increasing angle"
            }

            for (sides in 3..6) {
                val original = when (type) {
                    5 -> ShapeContour.fromPoints(List(sides * 2) {
                        val i = kotlin.math.abs(it - sides)
                        val k = if (it < sides) 1.0 else 1.4
                        (i * (360 / sides)).toCartesian() * k
                    }, true)
                    4 -> ShapeContour.fromPoints(List(sides * 2) {
                        val i = kotlin.math.abs(it - sides + 0.5).toInt()
                        val k = if (it < sides) 1.0 else 1.4
                        (i * (360 / sides)).toCartesian() * k
                    }, true)
                    3 -> contour {
                        moveTo(0.toCartesian())
                        for (it in 1..sides) {
                            curveTo(
                                ((it * 2 - 1) * (180 / sides)).toCartesian(),
                                (it * (360 / sides)).toCartesian()
                            )
                        }
                        close()
                    }
                    2 -> contour {
                        moveTo(0.toCartesian())
                        for (it in sides - 1 downTo 0) {
                            curveTo(
                                ((it * 2 + 1) * (180 / sides)).toCartesian(),
                                (it * (360 / sides)).toCartesian()
                            )
                        }
                        close()
                    }
                    1 -> ShapeContour.fromPoints(List(sides) {
                        ((sides - it) * (360 / sides)).toCartesian()
                    }, true)
                    else -> ShapeContour.fromPoints(List(sides) {
                        (it * (360 / sides)).toCartesian()
                    }, true)
                }

                curves.add(original)
                curves.add(original.offset(-10.0, SegmentJoin.MITER))
                curves.add(original.offset(10.0, SegmentJoin.MITER))
                curves.add(original.offset(-10.0, SegmentJoin.BEVEL))
                curves.add(original.offset(10.0, SegmentJoin.BEVEL))
                curves.add(original.offset(-10.0, SegmentJoin.ROUND))
                curves.add(original.offset(10.0, SegmentJoin.ROUND))
            }
        }

        populate(1)

        extend(Screenshots())

        extend {
            fun write(txt: String) {
                drawer.isolated {
                    fill = ColorRGBa.BLACK.opacify(0.8)
                    writer {
                        translate(-textWidth(txt) * 0.5, 75.0)
                        text(txt)
                    }

                }
            }

            with(drawer) {
                clear(ColorRGBa.WHITE)
                stroke = ColorRGBa(0.0, 0.0, 0.0, 0.05)
                fontMap = font
            }

            curves.forEachIndexed { i, c ->
                drawer.isolated {
                    val x = i % itemsPerSet
                    val y = i / itemsPerSet
                    translate(
                        width * (0.12 + x * 0.122),
                        height * (0.19 + y * 0.21)
                    )
                    fill = ColorRGBa.PINK.opacify(0.4)
                    contour(curves[itemsPerSet * (i / itemsPerSet)])
                    if (x > 0) {
                        fill = ColorRGBa.GREEN.opacify(0.4)
                        contour(c)
                    }
                    write(
                        when (i % itemsPerSet) {
                            0 -> "original"
                            1 -> "MITER -10.0"
                            2 -> "MITER 10.0"
                            3 -> "BEVEL -10.0"
                            4 -> "BEVEL 10.0"
                            5 -> "ROUND -10.0"
                            6 -> "ROUND 10.0"
                            else -> "?"
                        }
                    )
                }
            }
            drawer.isolated {
                translate(width * 0.5, -22.0)
                write(headline)
            }
        }

        keyboard.keyDown.listen {
            when (it.key) {
                KEY_ESCAPE -> exitProcess(0)
                else -> when (it.name) {
                    "0" -> populate(0)
                    "1" -> populate(1)
                    "2" -> populate(2)
                    "3" -> populate(3)
                    "4" -> populate(4)
                    "5" -> populate(5)
                }
            }
        }
    }
}
