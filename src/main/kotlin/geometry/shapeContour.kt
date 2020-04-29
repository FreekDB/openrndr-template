package geometry

import org.openrndr.extra.noise.Random
import org.openrndr.math.Vector2
import org.openrndr.math.clamp
import org.openrndr.math.map
import org.openrndr.shape.ShapeContour
import java.lang.Integer.min

// From ofPolyline.inl in openFrameworks
// `smoothingSize` is the size of the smoothing window. So if
// `smoothingSize` is 2, then 2 points from the left, 1 in the center,
// and 2 on the right (5 total) will be used for smoothing each point.
// `smoothingShape` describes whether to use a triangular window (0) or
// box window (1) or something in between (for example, .5).

fun ShapeContour.smoothed(smoothingSize: Int, smoothingShape: Double = 0.0): ShapeContour {
    val n = segments.size
    val sSize = clamp(smoothingSize, 0, n);
    val sShape = clamp(smoothingShape, 0.0, 1.0);

    // precompute weights and normalization
    var weights = List(sSize) {
        map(0.0, sSize * 1.0, 1.0, sShape, it * 1.0)
    }

    val result = MutableList(n) { segments[it].start }

    for (i in 0 until n) {
        var sum = 1.0 // center weight
        for (j in 1 until sSize) {
            var cur = Vector2.ZERO
            var leftPosition = i - j
            var rightPosition = i + j
            if (leftPosition < 0 && closed) {
                leftPosition += n
            }
            if (leftPosition >= 0) {
                cur += segments[leftPosition].start;
                sum += weights[j]
            }
            if (rightPosition >= n && closed) {
                rightPosition -= n
            }
            if (rightPosition < n) {
                cur += segments[rightPosition].start
                sum += weights[j]
            }
            result[i] += cur * weights[j]
        }
        result[i] = result[i] / sum
    }

    return ShapeContour.fromPoints(result, closed)
}

// TODO: have two versions of this? closed one is fine
// but open one should have no distortion at the ends
// and increase in the middle with cosine
fun ShapeContour.noisified(distance: Int, closed: Boolean = true, zoom: Double = 0.002): ShapeContour {
    return ShapeContour.fromPoints(List(this.segments.size + 1) {
        if(it != this.segments.size) {
            val seg = this.segments[it]
            val p = seg.start
            val n = seg.normal(0.0)
            p + n * (Random.perlin(p.x * zoom, p.y * zoom) * distance * 3)
        } else {
            this.segments[it-1].end
        }
    }, closed)
}
