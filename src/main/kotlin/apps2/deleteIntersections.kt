package apps2

import aBeLibs.geometry.removeIntersections
import org.openrndr.KEY_ENTER
import org.openrndr.KEY_ESCAPE
import org.openrndr.KEY_INSERT
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.dialogs.saveFileDialog
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.Random
import org.openrndr.math.CatmullRomChain2
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.shape.CompositionDrawer
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.bounds
import org.openrndr.svg.writeSVG
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.system.exitProcess

/**
 * .removeIntersections(10.0) is currently broken.
 */
fun main() = application {
    configure {
        width = 1200
        height = 554
    }
    program {
        Random.seed = System.currentTimeMillis().toString()

        var svg = CompositionDrawer()
        val contours = mutableListOf<ShapeContour>()
        var centering = Vector2.ZERO

        fun makeRing(
            steps: Int, angleInc: Double, radius: Double,
            radiusDelta: Double, frq: Double, pos: Vector2
        ):
                List<ShapeContour> {

            val points = List(steps) {
                pos + Polar(
                    it * angleInc,
                    radius + radiusDelta * sin(it * frq)
                )
                    .cartesian
            }

            val cmr = CatmullRomChain2(points, 2.5, loop = true)

            // Use this curve to calculate the length and bounds of the curve
            val blueprint = ShapeContour.fromPoints(cmr.positions(200), true)

            // Create a scaling transform to cover % of the screen
//            val tr = transform {
//                scale(min(width / blueprint.bounds.width,
//                        height / blueprint.bounds.height) * 0.5)
//            }
            // Get the length of the scaled curve
            val len = blueprint
                //.transform(tr)
                .length.roundToInt()
            println("len: $len")

            // Build a curve with so many points as the calculated length,
            // scale it.

            return listOf(ShapeContour.fromPoints(cmr.positions(len), true))
            //.transform(tr)
            //.removeSelfIntersections(10.0)
        }

        fun generate() {
            svg = CompositionDrawer()
            svg.fill = null
            svg.stroke = ColorRGBa.BLACK

            val p = drawer.bounds::position
            contours.clear()
            contours.addAll(
                makeRing(
                    15, 100.0, 100.0, 75.0, 0.42, // BL
                    p(0.328, 0.656)
                )
            )
            contours.addAll(
                makeRing(
                    12, 90.0, 95.0, 75.0, 0.6, // BR
                    p(0.669, 0.656)
                )
            )
            contours.addAll(
                makeRing(
                    20, 72.0, 95.0, 80.0, 0.32, // TL
                    p(0.156, 0.339)
                )
            )
            contours.addAll(
                makeRing(
                    10, 72.0, 134.0, 50.0, -1.7, // TM
                    p(0.510, 0.32)
                )
            )
            contours.addAll(
                makeRing(
                    40, 36.0, 90.0, 75.0, 0.44, // TR
                    p(0.842, 0.339)
                )
            )

            centering = (contours.map { it.bounds }).bounds.center

            //svg.translate(drawer.bounds.center - centering)
            svg.strokeWeight = 2.0
            svg.contours(contours)
            // BROKEN
            //svg.contours(contours.removeIntersections(10.0))
        }
        generate()

        extend(Screenshots())
        extend {
            drawer.run {
                clear(ColorRGBa.PINK)
                stroke = ColorRGBa.BLACK
                translate(drawer.bounds.center - centering)
                composition(svg.composition)
            }
        }
        keyboard.keyDown.listen {
            when (it.key) {
                KEY_ENTER -> generate()
                KEY_INSERT ->
                    saveFileDialog(supportedExtensions = listOf("svg")) { f ->
                        f.writeText(writeSVG(svg.composition))
                    }
                KEY_ESCAPE -> exitProcess(0)
            }
        }
    }
}
