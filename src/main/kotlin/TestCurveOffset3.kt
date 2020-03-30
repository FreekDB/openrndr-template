import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolated
import org.openrndr.draw.loadFont
import org.openrndr.extensions.Screenshots
import org.openrndr.math.Polar
import org.openrndr.shape.SegmentJoin
import org.openrndr.shape.ShapeContour
import org.openrndr.text.writer
import kotlin.system.exitProcess

/**
 * Testing recent changes to ShapeContour and .offset()
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

        for (sides in 3..6) {
            val original = ShapeContour.fromPoints(List(sides) {
                Polar(it * (360.0 / sides), 50.0).cartesian
            }, true)
            curves.add(original)
            curves.add(original.offset(-10.0, SegmentJoin.MITER))
            curves.add(original.offset(10.0, SegmentJoin.MITER))
            curves.add(original.offset(-10.0, SegmentJoin.BEVEL))
            curves.add(original.offset(10.0, SegmentJoin.BEVEL))
            curves.add(original.offset(-10.0, SegmentJoin.ROUND))
            curves.add(original.offset(10.0, SegmentJoin.ROUND))
        }

        extend(Screenshots())

        extend {
            drawer.background(ColorRGBa.WHITE)
            drawer.stroke = ColorRGBa(0.0, 0.0, 0.0, 0.05)
            drawer.fill = ColorRGBa.PINK.opacify(0.4)
            drawer.fontMap = font

            fun write(txt: String) {
                drawer.isolated {
                    drawer.fill = ColorRGBa.BLACK.opacify(0.8)
                    writer {
                        drawer.translate(-textWidth(txt) * 0.5, 75.0)
                        text(txt);
                    }

                }
            }

            curves.forEachIndexed {i, c ->
                drawer.isolated {
                    drawer.translate(
                        width * (0.12 + (i % itemsPerSet) * 0.122),
                        height * (0.15 + (i / itemsPerSet) * 0.22)
                    )
                    drawer.contour(curves[itemsPerSet * (i / itemsPerSet)])
                    drawer.contour(c)
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
        }

        keyboard.keyDown.listen {
            if (it.key == KEY_ESCAPE) {
                exitProcess(0)
            }
        }

    }
}
