package axi

import aBeLibs.extensions.NoJitter
import aBeLibs.extensions.TransRotScale
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.dialogs.saveFileDialog
import org.openrndr.draw.LineCap
import org.openrndr.draw.LineJoin
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.gui.GUI
import org.openrndr.extra.noise.Random
import org.openrndr.extra.parameters.*
import org.openrndr.extras.color.presets.DARK_SALMON
import org.openrndr.extras.color.presets.DARK_TURQUOISE
import org.openrndr.math.Polar
import org.openrndr.shape.*
import org.openrndr.svg.saveToFile
import org.openrndr.utils.namedTimestamp
import java.io.File

fun main() {
    application {
        configure {
            width = 1224
            height = 1024
        }

        program {
            Random.seed = System.currentTimeMillis().toString()

            val svg = drawComposition { }

            val A = @Description("Config A") object {
                @DoubleParameter("radius", 20.0, 250.0, order = 0)
                var radius = 100.0

                @DoubleParameter("xPos", 0.0, 0.5, order = 1)
                var xPos = 1 / 3.0

                @DoubleParameter("angle offset", -180.0, 180.0, order = 2)
                var angleOffset = 0.0
            }
            val B = @Description("Config B") object {
                @DoubleParameter("radius", 20.0, 250.0, order = 0)
                var radius = 140.0

                @DoubleParameter("xPos", 0.5, 1.0, order = 1)
                var xPos = 2 / 3.0

                @DoubleParameter("angle offset", -180.0, 180.0, order = 2)
                var angleOffset = 140.0
            }
            val settings = @Description("Settings") object {
                @IntParameter("count", 10, 200, order = 0)
                var count = 50

                @DoubleParameter("min normal", 10.0, 300.0, order = 1)
                var minNormal = 50.0

                @DoubleParameter("max normal", 10.0, 300.0, order = 2)
                var maxNormal = 150.0

                @DoubleParameter("normal mult", 0.5, 2.0, order = 3)
                var normalMult = 1.0

                @BooleanParameter("color", order = 4)
                var useColor = true
            }

            fun newDesign() {
                val c0 = drawer.bounds.position(A.xPos, 0.5)
                val c1 = drawer.bounds.position(B.xPos, 0.5)
                val seg = LineSegment(c0, c1)
                val segCenter = seg.position(0.5)
                val norm1 = seg.normal

                val groups = List(2) {
                    mutableListOf<Pair<Double, Segment>>()
                }

                val count = settings.count
                repeat(count) {
                    val angle = it * 360.0 / count
                    val start = c0 + Polar(
                        A.angleOffset + angle, A.radius
                    ).cartesian
                    val end = c1 + Polar(
                        B.angleOffset - angle, B.radius
                    ).cartesian
                    val dist = start.distanceTo(end)
                    val control0 = start + (start - c0).normalized * dist
                    val control1 = end + (end - c1).normalized * dist

                    // Figure out which ones are above and which ones
                    // below. Color them to distinguish them
                    val curve = Segment(start, control0, control1, end)

                    val curveCenter = curve.position(0.5)
                    val nearest = seg.nearest(curveCenter)
                    val norm2 = (curveCenter - nearest).normalized
                    val above = norm1.dot(norm2) > 0.0

                    // Make two partitions, the ones above `seg`
                    // and the ones below `seg`.
                    groups[if (above) 0 else 1].add(angle to curve)
                }
                // In each partition, find the index of a possible
                // discontinuity
                val discontinuityIndex = groups.map { list ->
                    list.map { it.first }.zipWithNext()
                        .indexOfFirst { pair ->
                            pair.second - pair.first > 90.0
                        } + 1
                }

                // Sort each partition properly so the angle of all items
                // are sorted clockwise, even when crossing the 0~360
                // discontinuity.
                // That means in some cases do nothing, in other cases
                // swap the first items with the last items in the list
                val continuous = groups.mapIndexed { i, list ->
                    if (discontinuityIndex[i] > 0) {
                        list.subList(discontinuityIndex[i], list.size) +
                                list.subList(0, discontinuityIndex[i])
                    } else {
                        list
                    }
                }

                // If the partition starts near segCenter, reverse it.
                // We want each list to start with the far away segments.
                val sorted = continuous.map {
                    val a = it.first().second.start
                    val b = it.last().second.start
                    if (b.squaredDistanceTo(segCenter) < a
                            .squaredDistanceTo(segCenter)
                    ) {
                        it.reversed()
                    } else {
                        it
                    }
                }

                // Finally iterate through the segments in each partition,
                // starting with the outer ones.
                svg.clear()
                svg.draw {
                    stroke = ColorRGBa.BLACK
                    sorted.forEachIndexed { groupId, it ->
                        it.forEachIndexed { i, (_, s) ->
                            val iNorm = (0.5 + i) / it.size
                            if (settings.useColor) stroke =
                                (if (groupId == 0) ColorRGBa.DARK_SALMON else
                                    ColorRGBa.DARK_TURQUOISE).shade(iNorm)

                            val sCenter = (s.start + s.end) / 2.0
                            val mag = s.length * 0.5
                            val m = sCenter + norm1 * mag * iNorm *
                                    if (groupId == 0) 1.0 else -1.0
                            val c = (s.end - s.start).normalized * mag *
                                    (0.1 + iNorm) * settings.normalMult

                            // Convert the segment into two segments
                            val k = settings.minNormal + (settings.maxNormal
                                    - settings.minNormal) * iNorm
                            contour(
                                ShapeContour(
                                    listOf(
                                        Segment(
                                            s.start,
                                            s.start + s.direction(0.0) * k,
                                            m - c, m
                                        ),
                                        Segment(
                                            m, m + c,
                                            s.end - s.direction(1.0) * k,
                                            s.end
                                        )
                                    ), false
                                )
                            )
                        }
                    }

                }
            }

            val gui = GUI().apply {
                compartmentsCollapsedByDefault = false
            }

            @Suppress("unused")
            val actions = @Description("Actions") object {
                @ActionParameter("new design", order = 0)
                fun update() = newDesign()

                @ActionParameter("svg", order = 1)
                fun svg() = saveFileDialog(supportedExtensions = listOf("svg")) { file ->
                    svg.saveToFile(file)
                }

                @ActionParameter("exit", order = 2)
                fun quit() = application.exit()
            }
            extend(gui) {
                add(actions)
                add(A)
                add(B)
                add(settings)
            }
            extend(NoJitter())
            extend(TransRotScale())
            extend(Screenshots())
            extend {
                drawer.clear(ColorRGBa.WHITE)
                drawer.lineCap = LineCap.ROUND
                drawer.lineJoin = LineJoin.ROUND
                drawer.composition(svg)
            }

        }
    }
}
