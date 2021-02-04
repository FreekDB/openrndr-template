package apps

import aBeLibs.geometry.noisified
import aBeLibs.geometry.smoothed
import org.openrndr.*
import org.openrndr.color.ColorRGBa
import org.openrndr.color.ColorXSVa
import org.openrndr.dialogs.saveFileDialog
import org.openrndr.draw.isolated
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.Random
import org.openrndr.extra.noise.perlin
import org.openrndr.extra.noise.simplex
import org.openrndr.math.Polar
import org.openrndr.math.mix
import org.openrndr.shape.CompositionDrawer
import org.openrndr.shape.ShapeContour
import org.openrndr.svg.writeSVG
import kotlin.math.*
import kotlin.system.exitProcess

/*
  1. Create a number of parallel curves.
  2. Create undulating waves between those curves.
  3. Duplicate the wavy curves multiple times with a slight offset to produce variable-thickness lines

  From my book, pages 7 and 15.
 */

fun main() = application {
    configure {
        width = 15 * 80
        height = 12 * 80
    }

    program {
        var center = drawer.bounds.center
        var rotation = 0.0
        var bgcolor = ColorRGBa.PINK

        val hairContours = mutableListOf<ShapeContour>()
        var pointCount = 200
        val smoothingSize = 5

        fun ShapeContour.smoothOffset(d: Double) = this.offset(d)
            .sampleEquidistant(pointCount).smoothed(smoothingSize)

        fun populate() {
            val seed = System.currentTimeMillis().toInt()

            hairContours.clear()
            hairContours.add(ShapeContour.fromPoints(List(pointCount) {
                val a = 2 * PI * it / pointCount
                Polar(
                    Math.toDegrees(a),
                    200.0 + 10.0 * simplex(seed, cos(a), sin(a))
                ).cartesian
            }, true).sampleEquidistant(pointCount).smoothed(smoothingSize)) // 0
            hairContours.add(hairContours[0].smoothOffset(20.0)) // 1
            hairContours.add(hairContours[1].smoothOffset(40.0)) // 2
            hairContours.add(hairContours[2].smoothOffset(60.0)) // 3
            hairContours.add(hairContours[3].smoothOffset(80.0)) // 4
            hairContours.add(hairContours[0].smoothOffset(-20.0)) // 5
            hairContours.add(hairContours[5].smoothOffset(-20.0)) // 6
            hairContours.add(hairContours[6].smoothOffset(-20.0)) // 7
            hairContours.add(hairContours[7].smoothOffset(-20.0)) // 8

            // Make waves between the guide lines
            pointCount *= 6
            for (k in listOf(
                Pair(0, 1),
                Pair(2, 1),
                Pair(2, 3),
                Pair(4, 3),
                Pair(0, 5),
                Pair(6, 5),
                Pair(6, 7),
                Pair(8, 7)
            )) {
                hairContours.add(ShapeContour.fromPoints(List(pointCount) {
                    val pc = it * 1.0 / pointCount
                    val x = pc * 120 + perlin(seed, sin(1 * pc * PI * 2),
                        0.0) * 20.0
                    // 1. simple sine wave
                    // val sin = sin(theta * waveCount + waveOffset + Random.perlin(sin(theta), 0.0)) * 0.4 + 0.5

                    // 2. semicircular wave
                    // https://math.stackexchange.com/questions/44329/function-for-concatenated-semicircles
                    val sin =
                        (-1.0).pow(floor(x / 2 + 0.5)) * sqrt(
                            1 - (x - 2 * floor(x / 2 + 0.5)).pow(2.0)
                        ) * 0.4 + 0.5
                    mix(
                        hairContours[k.first].position(pc),
                        hairContours[k.second].position(pc),
                        sin
                    )
                }, true))
            }

            // Remove guidelines
            hairContours.removeIf { it.segments.size < 200 }

            // Clone all lines N times, distort them using a noise function
            // so part of the lines are not distorted and other parts are more, creating thick lines
            val lineCount = hairContours.size

            // 04.02.2021: lineCount = 17 so lineId below goes too high.
            // I added a % 9 to keep it in bounds. But when did this stop
            // working? ALSO: there's a glitch in the unions of the closed
            // contours.
            for (lineId in 0 until lineCount) {
                for (i in 0 until listOf(5, 6, 7, 8, 4, 3, 2, 1, 0)[lineId % 9]) {
                    hairContours.add(hairContours[lineId].noisified(i))
                }
            }
        }

        populate()

        fun exportSVG() {
            val svg = CompositionDrawer()
            svg.translate(drawer.bounds.center)
            svg.fill = null
            svg.stroke = ColorRGBa.BLACK
            svg.contours(hairContours)
            saveFileDialog(supportedExtensions = listOf("svg")) {
                it.writeText(writeSVG(svg.composition))
            }
        }

        extend(Screenshots())
        extend {
            drawer.clear(bgcolor)

            drawer.isolated {
                drawer.translate(center)
                drawer.rotate(rotation)
                drawer.fill = null
                drawer.stroke = ColorRGBa(0.0, 0.0, 0.0, 0.5)
                drawer.contours(hairContours)
            }
        }

        mouse.dragged.listen {
            if (mouse.pressedButtons.contains(MouseButton.LEFT)) {
                center += it.dragDisplacement
            }
            if (mouse.pressedButtons.contains(MouseButton.RIGHT)) {
                rotation += it.dragDisplacement.x
            }
        }

        keyboard.keyDown.listen {
            when (it.key) {
                KEY_ESCAPE -> exitProcess(0)
                KEY_INSERT -> exportSVG()
                KEY_ENTER -> {
                    bgcolor =
                        ColorXSVa(Random.double0(360.0), 0.3, 0.95).toRGBa()
                    populate()
                }
            }
        }
    }
}


