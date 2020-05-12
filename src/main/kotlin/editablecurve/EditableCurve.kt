package editablecurve

import geometry.intersects
import geometry.makeParallelCurve
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.extra.noise.Random.simplex
import org.openrndr.extra.noise.uniform
import org.openrndr.math.Vector2
import org.openrndr.shape.ContourBuilder
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.contour
import kotlin.math.abs

class EditableCurve {
    companion object {
        const val pointCount = 4
        const val pointRadius = 10.0
        var curveResolution = 60.0
        var colorEdit = ColorRGBa.YELLOW
    }

    private var controlPoints = MutableList(4) {
        Vector2.ZERO
    }
    private var separations = mutableListOf<Double>()
    var numSubcurves = 1
    var separation = 25.0
    @Transient
    private var line: ShapeContour = ShapeContour(emptyList(), false)
    @Transient
    private var activePoint = pointCount

    fun update() {
        line = contour {
            moveTo(controlPoints[0])
            curveTo(controlPoints[1], controlPoints[2], controlPoints[3])
        } //.sampleEquidistant(curveResolution.toInt())
    }

    fun distanceTo(p: Vector2): Double {
        val lineCopy = line.sampleEquidistant(10)
        return lineCopy.segments.map { (it.start - p).squaredLength }.min()!!
    }

    fun randomize(screenSize: Vector2) {
        for (i in 0 until pointCount) {
            controlPoints[i] = Vector2.uniform(
                Vector2(screenSize.x * 0.1, screenSize.x * 0.1),
                Vector2(screenSize.x * 0.9, screenSize.y * 0.9)
            )
        }
        update()
    }

    fun draw(drawer: Drawer, editing: Boolean) {
        if (editing) {
            drawer.stroke = colorEdit

            controlPoints.forEach {
                drawer.circle(it, pointRadius)
            }
            drawer.lineSegment(controlPoints[0], controlPoints[1])
            drawer.lineSegment(controlPoints[2], controlPoints[3])
        } else {
            drawer.stroke = colorEdit.opacify(0.3)
        }
        drawer.contour(line)
    }

    fun mousePressed(p: Vector2) {
        activePoint = pointCount;
        for (i in 0 until pointCount) {
            val dist = (p - controlPoints[i]).length
            if (dist < pointRadius) {
                activePoint = i
                break
            }
        }
    }

    fun mouseDragged(p: Vector2) {
        if (activePoint < pointCount) {
            controlPoints[activePoint] = p
        }
        update()
    }

    fun addSegmentsTo(segments: MutableList<ShapeContour>) {
        if (separations.size > numSubcurves) {
            separations = separations.subList(0, numSubcurves)
        }
        while (separations.size < numSubcurves) {
            separations.add(Math.random());
        }
        var dist = 0.0;
        val lineCopy = line.sampleEquidistant(curveResolution.toInt())
        for (i in 0 until numSubcurves) {
            val sep = separations[i]
            val start = 0.02 + abs(0.2 * simplex(i * 0.03, 7.0))
            val end = 0.98 - abs(0.2 * simplex(i * 0.03, 3.0))
            dist += 1 + separation * sep * sep * sep
            //val c2 = makeParallelCurve(line.sub(start, end), dist)
            val c2 = lineCopy.sub(start, end).makeParallelCurve(dist)
            addSegmentsOfLineTo(c2, segments)
        }
    }

    // IDEA: Have a method that returns all intersections between two lines
    // Also a method that returns all intersections between one line and a set of lines
    // This can be optimized: if the bounding boxes don't intersect, we are done.
    // Another possible optimization is comparing line segments with bounding boxes. That potentially
    // saves a lot of computation: but how do you know if a line cuts through a box? By comparing
    // if the first line intersects with any of the 4 lines defining the bounding box.
    private fun addSegmentsOfLineTo(l: ShapeContour, segments: MutableList<ShapeContour>) {
        var builder = ContourBuilder(true)

        var drawing = true;
        l.segments.indices.forEach { i ->
            var intersection = Vector2.INFINITY
            // test for intersections against
            // all other segments in all other lines
            for (segment in segments) {
                intersection = segment.intersects(l.segments[i])
                if (intersection != Vector2.INFINITY) {
                    break
                }
            }

            if (drawing) {
                builder.moveOrLineTo(l.segments[i].start)
            }
            if (intersection != Vector2.INFINITY) {
                if (drawing) {
                    drawing = false
                    builder.moveOrLineTo(intersection)
                    segments.add(ShapeContour(builder.segments, false))
                } else {
                    drawing = true
                    builder = ContourBuilder(true)
                    builder.moveOrLineTo(intersection)
                }
            }
        }
        if (drawing) {
            builder.moveOrLineTo(l.segments.last().end)
            segments.add(ShapeContour(builder.segments, false))
        }
    }
}