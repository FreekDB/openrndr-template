package apps.simpleTests

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.dialogs.saveFileDialog
import org.openrndr.draw.isolated
import org.openrndr.draw.loadFont
import org.openrndr.draw.writer
import org.openrndr.extensions.Screenshots
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.math.mix
import org.openrndr.shape.CompositionDrawer
import org.openrndr.shape.contours
import org.openrndr.shape.shape
import org.openrndr.svg.writeSVG
import kotlin.math.sin

/**
 * id: 53599ba9-69a4-4b0d-9df5-2b9337371e6d
 * description: Test simple contour cases in screen vs SVG.
 * tags: #new
 */

fun main() = application {
    configure {
        width = 1100
        height = 300
    }

    program {
        extend(Screenshots())

        val font = loadFont("data/fonts/IBMPlexMono-Regular.ttf", 16.0)
        val svg = CompositionDrawer()

        val pts = listOf(
            Vector2(0.0, 0.0),    // 0  0->1=Horz, 1->2=Vert
            Vector2(50.0, 0.0),   // 1
            Vector2(50.0, 50.0),  // 2
            Vector2(50.0, 100.0), // 3 3->4=Horz, 4->5=Vert
            Vector2(0.0, 100.0),  // 4
            Vector2(0.0, 60.0)    // 5
        )

        /**
         * Screen: one might expect that moveTo does not draw anything,
         * but after calling lineTo(), moveTo() is equivalent to lineTo()
         *
         * SVG: it seems like the second moveTo() is ignored. The expected
         * result is two horizontal lines.
         */
        val contourEquals =
            contours {
                moveTo(pts[0])
                lineTo(pts[1])
                moveTo(pts[3])
                lineTo(pts[4])
            }

        /**
         * Draws correctly in SVG
         * Invisible on screen
         */
        val shapeEquals = shape {
            contour {
                moveTo(pts[0])
                lineTo(pts[1])
                close() // ADDED
            }
            contour {
                moveTo(pts[3])
                lineTo(pts[4])
                close() // ADDED
            }
        }

        /**
         * Correct on SVG, on the screen it seems
         * to draw the vertical segment (1,2) and
         * the horizontal segment (3,4)
         * The  last two points in the first  segment
         * The first two points in the second segment
         */
        val shapeTwoL = shape {
            contour {
                moveTo(pts[0])
                lineTo(pts[1])
                lineTo(pts[2])
                close() // ADDED
            }
            contour {
                moveTo(pts[3])
                lineTo(pts[4])
                lineTo(pts[5])
                close() // ADDED
            }
        }

        /**
         * Closed shapes look correct and identical on screen and SVG.
         */
        val shapeSandclock = shape {
            contour {
                moveTo(pts[0])
                lineTo(pts[1])
                lineTo(pts[2])
                close()
            }
            contour {
                moveTo(pts[3])
                lineTo(pts[4])
                lineTo(pts[5])
                close()
            }
        }

        /**
         * When drawing multiple open contours inside a shape (not calling close() )
         * the contours are still closed on screen, but open on SVG.
         */
        val shapeUnclosedLines = shape {
            (0 until 360 step 36).forEach { angle ->
                val p0 = Polar(angle + 5.0, 30.0).cartesian
                val p1 = Polar(angle + 8.0, 60.0).cartesian
                contour {
                    moveTo(p0)
                    (1..10).forEach {
                        lineTo(
                            mix(p0, p1, it / 10.0) +
                                    Polar(angle + 90.0, 100.0 * sin(it * 0.3)).cartesian
                        )
                    }
                }
            }
        }

        fun exportSVG() {
            svg.fill = null
            svg.stroke = ColorRGBa.BLACK
            svg.translate(100.0, 150.0)
            svg.contours(contourEquals)
            svg.translate(200.0, 0.0)
            svg.shape(shapeEquals)
            svg.translate(200.0, 0.0)
            svg.shape(shapeTwoL)
            svg.translate(200.0, 0.0)
            svg.shape(shapeSandclock)
            svg.translate(200.0, 0.0)
            svg.shape(shapeUnclosedLines)
            saveFileDialog(supportedExtensions = listOf("svg")) {
                it.writeText(writeSVG(svg.composition))
            }
        }

        fun write(txt: String) {
            drawer.isolated {
                fontMap = font
                fill = ColorRGBa.BLACK
                writer {
                    translate(-textWidth(txt) * 0.5, 135.0)
                    text(txt)
                }

            }
        }

        exportSVG()

        extend {
            drawer.clear(ColorRGBa.WHITE)

            drawer.stroke = ColorRGBa(0.0, 0.0, 0.0, 0.5)
            drawer.fill = null

            drawer.translate(100.0, 150.0)
            drawer.contours(contourEquals)
            write("contour equals")

            drawer.translate(200.0, 0.0)
            drawer.shape(shapeEquals)
            write("shape equals")

            drawer.translate(200.0, 0.0)
            drawer.shape(shapeTwoL)
            write("shape two L")

            drawer.translate(200.0, 0.0)
            drawer.shape(shapeSandclock)
            write("shape sandclock")

            drawer.translate(200.0, 0.0)
            drawer.shape(shapeUnclosedLines)
            write("shape unclosed lines")
        }
    }
}
