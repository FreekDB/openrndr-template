package apps2

import geometry.intersects
import org.openrndr.KEY_ENTER
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.dialogs.saveFileDialog
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.Random
import org.openrndr.math.CatmullRomChain2
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.math.transforms.transform
import org.openrndr.shape.CompositionDrawer
import org.openrndr.shape.SegmentJoin
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.rectangleBounds
import org.openrndr.svg.writeSVG
import kotlin.math.min
import kotlin.math.roundToInt

fun main() = application {
    configure {
        width = 1024
        height = 1024
    }
    program {
        Random.seed = System.currentTimeMillis().toString()

        var svg = CompositionDrawer()
        val contours = mutableListOf<ShapeContour>()
        var centering = Vector2.ZERO
        //val intersections = mutableListOf<Vector2>()

        fun generate() {
            contours.clear()
            //intersections.clear()

//        val n = 5
//        val points = List(n * 2) {
//            val a = (it / 2) * (360.0 / n)
//            val b = (it % 2) * 20.0
//            Polar(a - b, 160.0 - b).cartesian + drawer.bounds.center
//        }
            //val points = List(6) { drawer.bounds.randomPoint() }

            val cores = Random.int(2, 5)
            val points = mutableListOf<Vector2>()
            for (i in 0 until cores) {
                val center = drawer.bounds.position(Random.double(0.2, 0.8), Random.double(0.2, 0.8))
                val count = Random.int(6, 20)
                val startAngle = Random.double0(360.0)
                for (j in 0 until count) {
                    points.add(
                        center + Polar(startAngle + j * 35.0, Random.double(50.0, 100.0)).cartesian
                    )
                }
            }
            val cmr = CatmullRomChain2(points, 2.5, loop = true)

            // Use this curve to calculate the length and bounds of the curve
            var blueprint = ShapeContour.fromPoints(cmr.positions(200), true)
            // Create a scaling transform to cover 80% of the screen
            val tr = transform {
                scale(min(width / blueprint.bounds.width, height / blueprint.bounds.height) * 0.8)
            }
            // Get the length of the scaled curve
            val len = blueprint.transform(tr).length.roundToInt()
            println("len: $len")

            // Now build a curve with so many points as the calculated length, scale it.
            val uncut = ShapeContour.fromPoints(cmr.positions(len / 4), true).transform(tr)

            for(copy in 0 until 4) {
                val displaced = if(copy == 0) {
                    uncut
                } else {
                    uncut.offset(copy * 5.0, SegmentJoin.MITER)
                }
                // For the new contour find all self intersections (the normalized positions in the curve)
                val intPcs = displaced.selfIntersections()
                println("Intersections: ${intPcs.size}")
                val margin = 20.0
                if (intPcs.isNotEmpty()) {
                    for (i in intPcs.indices step 2) {
                        if (i < intPcs.size - 3) {
                            contours.add(displaced.sub(intPcs[i], intPcs[i + 2]).shorten(margin))
                        } else {
                            contours.add((displaced.sub(intPcs[i], 1.0) + displaced.sub(0.0, intPcs[0])).shorten(margin))
                        }
                    }
                    //intersections.addAll(intPcs.map { displaced.position(it) })
                } else {
                    contours.add(displaced)
                }
            }

            centering = rectangleBounds(contours.map { it.bounds }).center

            svg = CompositionDrawer()
            svg.fill = null
            svg.stroke = ColorRGBa.BLACK
            //svg.translate(drawer.bounds.center - centering)
            svg.strokeWeight = 2.0
            svg.contours(contours)
        }
        generate()

        extend(Screenshots())
        extend {
            drawer.run {
                clear(ColorRGBa.WHITE)
                translate(drawer.bounds.center - centering)
                composition(svg.composition)
            }
        }
        keyboard.keyDown.listen {
            if (it.key == KEY_ENTER) {
                generate()
            }
            if (it.name == "s") {
                saveFileDialog(supportedExtensions = listOf("svg")) { f ->
                    f.writeText(writeSVG(svg.composition))
                }
            }
        }
    }
}

private fun ShapeContour.shorten(d: Double): ShapeContour {
    val step = 1.0 / length
    var startPc = 0.0
    var endPc = 1.0
    val start = position(startPc)
    val end = position(endPc)
    while (start.distanceTo(position(startPc)) < d && startPc < 0.4) {
        startPc += step
    }
    while (end.distanceTo(position(endPc)) < d && endPc > 0.6) {
        endPc -= step
    }
    return sub(startPc, endPc)
}

private fun ShapeContour.selfIntersections(): List<Double> {
    val result = mutableListOf<Double>()
    this.segments.forEachIndexed { i, seg ->
        val p = this.intersects(seg)
        if (p != Vector2.INFINITY) {
            result.add((i + (seg.on(p) ?: 0.0)) / (this.segments.size + 0.5))
        }
    }
    return result
}
