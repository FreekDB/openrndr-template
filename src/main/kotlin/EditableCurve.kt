import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.extra.noise.Random.simplex
import org.openrndr.extra.noise.uniform
import org.openrndr.math.Vector2
import org.openrndr.shape.ContourBuilder
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.contour
import org.openrndr.shape.intersection
import kotlin.math.PI
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
    private var numCopies = 1
    private var sepMultiplier = 25
    @Transient
    private var line: ShapeContour = ShapeContour(emptyList(), false)
    @Transient
    private var activePoint = EditableCurve.pointCount

    fun update() {
        line = contour {
            moveTo(controlPoints[0])
            curveTo(controlPoints[1], controlPoints[2], controlPoints[3])
        }.sampleEquidistant(curveResolution.toInt())
    }

    fun distanceTo(p: Vector2): Double {
        var minDist = Double.MAX_VALUE
        for (segment in line.segments) {
            val len = (segment.start - p).squaredLength
            if (len < minDist) {
                minDist = len
            }
        }
        return minDist
    }

    fun randomize() {
        for (i in 0 until pointCount) {
            controlPoints[i] = Vector2.uniform(
                Vector2.ZERO,
                Vector2(768.0, 576.0) // TODO: get screen dimensions
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

    // TODO: move to aBeLibs
    private fun makeParallelCurve(line: ShapeContour, dist: Double): ShapeContour {
        var points = mutableListOf<Vector2>()
        var prevNorm = Vector2.ZERO
        val len = line.segments.size.toDouble()
        line.segments.forEachIndexed { i, it ->
            val pc = i / len
            val wi = 0.5 - 0.5 * Math.cos(pc * PI * 2)
            val norm = (it.end - it.start).normalized.perpendicular
            points.add(it.start + (norm + prevNorm).normalized * wi * dist)
            prevNorm = norm
        }
        points.add(line.segments.last().end + prevNorm * 0.0 * dist)

        return ShapeContour.fromPoints(points, false)
    }

    fun addSegmentsTo(segments: MutableList<ShapeContour>) {
        if (separations.size > numCopies) {
            separations = separations.subList(0, numCopies)
        }
        while (separations.size < numCopies) {
            separations.add(Math.random());
        }
        var dist = 0.0;
        for (i in 0 until numCopies) {
            val sep = separations[i]
            val start = 0.02 + abs(0.2 * simplex(i * 0.03, 7.0))
            val end = 0.98 - abs(0.2 * simplex(i * 0.03, 3.0))
            dist += 1 + sepMultiplier * sep * sep * sep
            val c2 = makeParallelCurve(line.sub(start, end), dist)
            addSegmentsOfLineTo(c2, segments)
        }
    }

    // TODO: move to aBeLibs
    private fun intersects(p0: Vector2, p1: Vector2, shape: ShapeContour): Pair<Boolean, Vector2> {
        shape.segments.forEach {
            val isec = intersection(p0, p1, it.start, it.end)
            if (isec != Vector2.INFINITY) {
                return Pair(true, isec)
            }
        }
        return Pair(false, Vector2.ZERO)
    }

    // IDEA: Have a method that returns all intersections between two lines
    // Also a method that returns all intersections between one line and a set of lines
    // This can be optimized: if the bounding boxes don't intersect, we are done.
    // Another possible optimization is comparing line segments with bounding boxes. That potentially
    // saves a lot of computation: but how do you know if a line cuts through a box? By comparing
    // if the first line intersects with any of the 4 lines defining the bounding box.
    private fun addSegmentsOfLineTo(l: ShapeContour, segments: MutableList<ShapeContour>) {

        var builder = ContourBuilder()

        var drawing = true;
        l.segments.indices.forEach { i ->
            // get segment
            val pThis0 = l.segments[i].start
            val pThis1 = l.segments[i].end

            var intersection = Vector2(0.0)
            var isecFound: Boolean = false
            // test for intersections against
            // all other segments in all other lines
            for (segment in segments) {
                val (found, isec) = intersects(pThis0, pThis1, segment)
                if (found) {
                    isecFound = true
                    intersection = isec
                    break
                }
            }

            if (drawing) {
                builder.moveOrLineTo(pThis0)
            }
            if (isecFound) {
                if (drawing) {
                    drawing = false
                    builder.moveOrLineTo(intersection)
                    segments.add(ShapeContour(builder.segments, false))
                } else {
                    drawing = true
                    builder = ContourBuilder()
                    builder.moveOrLineTo(intersection)
                }
            }
        }
        if (drawing) {
            builder.moveOrLineTo(l.segments.last().end)
            segments.add(ShapeContour(builder.segments, false))
        }
    }

    fun setNumSubcurves(num: Int) {
        numCopies = num
    }

    fun setSep(sep: Int) {
        sepMultiplier = sep
    }

    fun getNumSubcurves(): Int {
        return numCopies
    }

    fun getSep(): Int {
        return sepMultiplier
    }
}