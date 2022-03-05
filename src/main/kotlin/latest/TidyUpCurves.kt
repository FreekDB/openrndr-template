package latest

import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.loadFont
import org.openrndr.draw.renderTarget
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.random
import org.openrndr.ffmpeg.VideoWriter
import org.openrndr.math.Polar
import org.openrndr.shape.Segment
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.intersections
import kotlin.math.max

/**
 * id: e3da455c-33c0-4c47-bb2d-9f54695d655f
 * description: A program that creates a collection of lines,
 * then mutates them randomly trying to create order.
 * tags: #separate
 */

fun main() = application {
    configure {
        width = 800
        height = 800
    }

    /**
     * Returns the number of intersections in [c]. I tried returning
     * the index of the [ShapeContour] with the highest number of intersections
     * to mutate that curve first, but it didn't help.
     */
    fun countIntersections(c: List<Segment>): Int {
        var total = 0
        for (i in c.indices) {
            for (j in i + 1 until c.size) {
                if (c[i].intersections(c[j]).isNotEmpty()) {
                    total++
                }
            }
        }
        return total
    }

    fun Segment.mutate(amount: Double): Segment {
        var mid1 = control[0] + Polar(random(360.0), random(amount)).cartesian
        var mid2 = control[1] + Polar(random(360.0), random(amount)).cartesian
        // Keep control points in bounds
        val maxDist = 290.0
        // TODO: I think this is wrong. Distance to what?
        if (mid1.length > maxDist) {
            mid1 = mid1.normalized * maxDist
        }
        if (mid2.length > maxDist) {
            mid2 = mid2.normalized * maxDist
        }
        return Segment(start, mid1, mid2, end)
    }

    program {
        val font = loadFont("data/fonts/SourceCodePro-Regular.ttf", 16.0)
        val videoWriter: VideoWriter? = null
//      val videoWriter = VideoWriter.create().size(width, height)
//      .profile(MP4Profile()).output("/tmp/tidyUpCurves.mp4").start()
        val videoTarget = renderTarget(width, height) {
            colorBuffer()
            depthBuffer()
        }
        val curves = MutableList(49) { i ->
            val a = 180 * i / 50.0
            val start = Polar(a, 300.0).cartesian
            val end = Polar(-a, 300.0).cartesian
            val mid1 = Polar(random(360.0), random(300.0)).cartesian
            val mid2 = Polar(random(360.0), random(300.0)).cartesian
            Segment(start, mid1, mid2, end)
        }
        extend(Screenshots())
        extend {
            var count = 0
            repeat(curves.size) { target ->
                val countBefore = countIntersections(curves)
                val backup = curves[target]
                val proposal = backup.mutate(max(30.0, countBefore * 1.0))
                curves[target] = proposal
                val countAfter = countIntersections(curves)
                if (countAfter > countBefore) {
                    curves[target] = backup
                }
                count = countBefore
            }
            drawer.isolatedWithTarget(videoTarget) {
                clear(ColorRGBa.BLACK)
                fontMap = font
                text("intersections: $count", 50.0, 70.0)
                text("frame: $frameCount", 50.0, 50.0)
                stroke = ColorRGBa.WHITE
                translate(bounds.center)
                segments(curves)
            }
            videoWriter?.frame(videoTarget.colorBuffer(0))
            drawer.image(videoTarget.colorBuffer(0))
        }
        keyboard.keyDown.listen {
            if (it.key == KEY_ESCAPE) {
                videoWriter?.stop()
                application.exit()
            }
        }
    }
}
