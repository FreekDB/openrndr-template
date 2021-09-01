package axi

import aBeLibs.data.ImageData
import aBeLibs.extensions.NoJitter
import aBeLibs.extensions.TransRotScale
import aBeLibs.math.cosEnv
import org.openrndr.KEY_ENTER
import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.dialogs.saveFileDialog
import org.openrndr.draw.LineCap
import org.openrndr.draw.LineJoin
import org.openrndr.exceptions.stackRootClassName
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.gui.GUI
import org.openrndr.extra.noise.Random
import org.openrndr.extra.parameters.*
import org.openrndr.extras.color.presets.DARK_SALMON
import org.openrndr.extras.color.presets.DARK_TURQUOISE
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.shape.*
import org.openrndr.svg.saveToFile


fun main() {
    application {
        configure {
            width = 1224
            height = 1024
            title = stackRootClassName()
        }

        program {
            Random.seed = System.currentTimeMillis().toString()

            val img = ImageData("/home/funpro/Pictures/n1/Instagram/")
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

                @DoubleParameter("image amount", 0.0, 30.0, order = 4)
                var imageAmount = 1.0

                @BooleanParameter("color", order = 5)
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
                            val sign = if (groupId == 0) 1.0 else -1.0
                            val m = sCenter + norm1 * mag * iNorm * sign
                            val c = (s.end - s.start).normalized * mag *
                                    (0.1 + iNorm) * settings.normalMult

                            // Convert the segment into two segments
                            val k = settings.minNormal + (settings.maxNormal
                                    - settings.minNormal) * iNorm

                            val cntr = ShapeContour(
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

                            // distort
                            val cPoints = cntr.equidistantPositions(1000)
                            val distorted = cPoints.mapIndexed { index, p ->
                                val amt = cosEnv(index * 1.0, 0.0, 999.0)
                                val pc = (index * 1.0) / 999.0
                                val color = img.getColor(
                                    Vector2(
                                        pc, iNorm * sign * 0.5 + 0.5
                                    )
                                )
                                p + Vector2(color.r, color.g) *
                                        (settings.imageAmount * amt)
                            }
                            val cntr2 = ShapeContour.fromPoints(
                                distorted,
                                false
                            )
                            contour(cntr2)
                        }
                    }

                }
            }

            val gui = GUI().apply {
                compartmentsCollapsedByDefault = false
            }

            @Suppress("unused")
            val actions = @Description("Actions") object {

                @ActionParameter("new design | ctrl+n", order = 0)
                fun aNewDesign() = newDesign()

                @ActionParameter("new image | enter", order = 1)
                fun newImage() = img.loadNext()

                @ActionParameter("svg | ctrl+s", order = 2)
                fun exportSVG() =
                    saveFileDialog(supportedExtensions = listOf("svg")) { file ->
                        svg.saveToFile(file)
                    }

                @ActionParameter("exit | esc", order = 3)
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
                drawer.lineJoin = LineJoin.BEVEL
                drawer.composition(svg)
            }
            keyboard.keyDown.listen {
                when (it.key) {
                    KEY_ESCAPE -> application.exit()
                    KEY_ENTER -> actions.newImage()
                }
                if (keyboard.pressedKeys.contains("left-control")) {
                    when (it.name) {
                        "s" -> actions.exportSVG()
                        "n" -> actions.aNewDesign()
                    }
                }
            }
        }
    }
}
