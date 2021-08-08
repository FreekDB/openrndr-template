package axi

import aBeLibs.extensions.TransRotScale
import aBeLibs.geometry.beautify
import aBeLibs.geometry.noisified
import aBeLibs.geometry.smoothed
import aBeLibs.math.semicircle
import org.openrndr.KEY_ENTER
import org.openrndr.KEY_ESCAPE
import org.openrndr.KEY_INSERT

import org.openrndr.color.ColorRGBa
import org.openrndr.draw.LineJoin
import org.openrndr.draw.isolated
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.Random
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.math.mix
import org.openrndr.shape.Segment
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.drawComposition
import org.openrndr.svg.saveToFile
import org.openrndr.utils.namedTimestamp
import java.io.File
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sin
import kotlin.system.exitProcess

/*
  1. Create a number of parallel curves.
  2. Create undulating waves between those curves.
  3. Duplicate the wavy curves multiple times with a slight offset to produce variable-thickness lines

  From my book, pages 7 and 15.
 */

fun main() = applicationSynchronous {
    configure {
        width = 1000
        height = 1000
    }

    program {
        var bgcolor = ColorRGBa.PINK

        val hairContours = mutableListOf<ShapeContour>()
        val smoothingSize = 5

        fun ShapeContour.smoothOffset(d: Double) = this.offset(d)
            .sampleEquidistant(600).smoothed(smoothingSize)

        fun wobblyContour(
            radius: Double,
            noisiness: Double,
            zoom: Double,
            pointCount: Int
        ) =
            ShapeContour.fromPoints(List(pointCount) {
                val circle = Polar(360.0 * it / pointCount).cartesian
                val noise = noisiness * Random.simplex(
                    circle.vector3(/*x = abs(circle.x),*/ z = radius * zoom)
                )
                drawer.bounds.center + circle * (radius * (1 + noise))
            }, true).smoothed(smoothingSize)

        fun cross(center: Vector2, radius: Double): List<ShapeContour> = listOf(
            Segment(
                center - Vector2(radius, 0.0),
                center + Vector2(radius, 0.0)
            ).contour,
            Segment(
                center - Vector2(0.0, radius),
                center + Vector2(0.0, radius)
            ).contour
        )

        fun populate() {
            Random.seed = System.currentTimeMillis().toString()

            hairContours.clear()
            val copies = 12
            repeat(copies) { curveId ->
                val progress = curveId / (copies - 1.0)
                val extremes = if (progress < 0.5)
                    (progress * 2).pow(2.0)
                else
                    1 - 0.5 * ((1 - progress) * 2).pow(2.0)
                val r0 = 200.0 + 300 * extremes
                val r1 = r0 + 30
                val noisiness = (0.5 - abs(0.5 * (1 - progress * 2))) * 0.6
                val a = wobblyContour(r0, noisiness, 0.0015, 200)
                val b = wobblyContour(r1, noisiness, 0.0015, 200)

                val pointCount = 1200
                hairContours.add(ShapeContour.fromPoints(List(pointCount) {
                    val linePc = it / pointCount.toDouble()
                    val noise =
                        Random.perlin(sin(linePc * PI * 2.0), progress * 30)
                    val x = linePc * 120 + noise * 20.0
                    val wave = 0.5 + semicircle(x) * 0.4
                    mix(a.position(linePc), b.position(linePc), wave)
                }, true).beautify())

                val last = hairContours.last()

                repeat(10) {
                    val z = 0.001 + it * 0.00004
                    hairContours.add(
                        last.noisified(it * 1.2 + 1.0, zoom = z).beautify()
                    )
                }
            }
            val center = drawer.bounds.center

            hairContours.addAll(cross(center, 50.0))
            hairContours.addAll(listOf(
                cross(center + Vector2(500.0), 20.0),
                cross(center + Vector2(500.0, -500.0), 20.0),
                cross(center + Vector2(-500.0, 500.0), 20.0),
                cross(center + Vector2(-500.0), 20.0)
            ).flatten())
        }

        populate()

        fun exportSVG() {
            val svg = drawComposition {
                translate(drawer.bounds.center)
                fill = null
                stroke = ColorRGBa.BLACK
                contours(hairContours)
            }
            svg.saveToFile(
                File(program.namedTimestamp("svg", "print"))
            )
        }

        extend(TransRotScale())
        extend(Screenshots())
        extend {
            drawer.clear(bgcolor)

            drawer.isolated {
                drawer.fill = null
                drawer.stroke = ColorRGBa(0.0, 0.0, 0.0, 0.5)
                drawer.lineJoin = LineJoin.BEVEL
                drawer.contours(hairContours)
            }
        }

        keyboard.keyDown.listen {
            when (it.key) {
                KEY_ESCAPE -> exitProcess(0)
                KEY_INSERT -> exportSVG()
                KEY_ENTER -> {
                    bgcolor = ColorRGBa.WHITE
                    //ColorXSVa(Random.double0(360.0), 0.3, 0.95).toRGBa()
                    populate()
                }
            }
        }
    }
}


