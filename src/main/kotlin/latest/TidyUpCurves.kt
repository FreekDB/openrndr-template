package latest

import aBeLibs.data.uniquePairs
import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.renderTarget
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.random
import org.openrndr.extra.videoprofiles.GIFProfile
import org.openrndr.ffmpeg.VideoWriter
import org.openrndr.math.Polar
import org.openrndr.shape.Segment
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.intersections

/**
 * A program that creates a collection of lines, then mutates them randomly
 * trying to create order
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

    fun Segment.mutate(): Segment {
        var mid1 = control[0] + Polar(random(360.0), random(20.0)).cartesian
        var mid2 = control[1] + Polar(random(360.0), random(20.0)).cartesian
        // Keep control points in bounds
        if(mid1.length > 290.0) {
           mid1 = mid1.normalized * 290.0
        }
        if(mid2.length > 290.0) {
            mid2 = mid2.normalized * 290.0
        }
        return Segment(start, mid1, mid2, end)
    }
    program {
        val curves = mutableListOf<Segment>()
        val videoWriter = VideoWriter
            .create()
            .size(width, height)
            .profile(GIFProfile())
            .output("/tmp/tidyUpCurves.gif")
            .start()
        val videoTarget = renderTarget(width, height) {
            colorBuffer()
            depthBuffer()
        }
        for (i in 1 until 50) {
            val a = 180 * i / 50.0
            val start = Polar(a, 300.0).cartesian
            val end = Polar(-a, 300.0).cartesian
            val mid1 = Polar(random(360.0), random(300.0)).cartesian
            val mid2 = Polar(random(360.0), random(300.0)).cartesian
            val seg = Segment(start, mid1, mid2, end)
            curves.add(seg)
        }
        extend(Screenshots())
        extend {
            repeat(curves.size) { target ->
                val countBefore = countIntersections(curves)
                val backup = curves[target]
                val proposal = backup.mutate()
                curves[target] = proposal
                val countAfter = countIntersections(curves)
                if (countAfter <= countBefore) {
                    drawer.isolatedWithTarget(videoTarget) {
                        clear(ColorRGBa.BLACK)
                        stroke = ColorRGBa.WHITE
                        translate(bounds.center)
                        segments(curves)
                    }
                    //videoWriter.frame(videoTarget.colorBuffer(0))
                } else {
                    curves[target] = backup
                }
            }
            drawer.image(videoTarget.colorBuffer(0))
        }
        keyboard.keyDown.listen {
            if (it.key == KEY_ESCAPE) {
                videoWriter.stop()
                application.exit()
            }
        }
    }
}