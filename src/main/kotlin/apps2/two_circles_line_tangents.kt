package apps2

import aBeLibs.geometry.*
import aBeLibs.math.angle
import aBeLibs.math.isAngleReflex
import org.openrndr.applicationSynchronous
import org.openrndr.color.ColorRGBa
import org.openrndr.color.mix
import org.openrndr.draw.LineCap
import org.openrndr.draw.LineJoin
import org.openrndr.math.Vector2
import org.openrndr.math.map
import org.openrndr.shape.Circle
import org.openrndr.shape.Segment

/**
 * Ported from
 * https://github.com/hamoid/Fun-Programming/tree/master/processing/ideas/2016/07/two_circles_line_tangents
 * then added concave and convex circle tangents.
 */

fun main() = applicationSynchronous {
    program {
        val leftCirc = Circle(drawer.bounds.position(0.2, 0.25), 100.0)
        val rightCirc = Circle(drawer.bounds.position(0.8, 0.75), 100.0)
//        val seg = Segment(drawer.bounds.position(0.7, 0.8), drawer.bounds.position(0.9, 0.9))
//        val font = loadFont("data/fonts/IBMPlexMono-Regular.ttf", 24.0)

        extend {
            drawer.lineJoin = LineJoin.BEVEL
            drawer.lineCap = LineCap.SQUARE
            val movingCirc = Circle(
                mouse.position.map(
                    Vector2.ZERO,
                    drawer.bounds.dimensions,
                    drawer.bounds.position(-0.2, -0.2),
                    drawer.bounds.position(1.2, 1.2)
                ), 50.0
            )
            drawer.run {
                fill = null
                clear(ColorRGBa.WHITE)
                stroke = ColorRGBa.GRAY
                circler(leftCirc)
                circler(rightCirc)
                circler(movingCirc)
                stroke = mix(ColorRGBa.BLUE, ColorRGBa.WHITE, 0.5)
//                c0.tangentLines(c1).forEach {
//                    lineSegment(it)
//                    circle(it.start, 5.0)
//                    circle(it.end, 5.0)
//                }
//                c0.intersections(c1).forEach {
//                    stroke = ColorRGBa.RED
//                    circle(it, 10.0)
//                }

//                stroke = mix(ColorRGBa.GREEN, ColorRGBa.BLACK, 0.5)
//                segment(seg)
//                seg.intersections(c1).forEach {
//                    circle(it, 6.0)
//                }

//                    fill = ColorRGBa.GRAY
//                    drawer.fontMap = font
//                    drawer.text("$case0 $a0 ", 50.0, 250.0)
//                    drawer.text("$case1 $a1", 50.0, 270.0)

                val convexPoints = mutableListOf<Vector2>()
                val convexRadius = 200.0
                leftCirc.tangentCirclesConvex(movingCirc, convexRadius).forEach { circle ->
                    drawer.strokeWeight = 1.0
                    circler(circle)
                    val s0 = Segment(
                        leftCirc.center,
                        leftCirc.center + (leftCirc.center - circle.center) * leftCirc.radius
                    )
                    segment(s0)
                    convexPoints.addAll(s0.intersections(leftCirc).map { it.position})

                    val s1 = Segment(
                        movingCirc.center,
                        movingCirc.center + (movingCirc.center - circle.center) * movingCirc.radius
                    )
                    segment(s1)
                    convexPoints.addAll(s1.intersections(movingCirc).map { it.position})
                }

                if (convexPoints.size == 4) {
                    val a0 = angle(leftCirc.center, convexPoints[0], convexPoints[2])
                    val a1 = angle(movingCirc.center, convexPoints[2], convexPoints[2])

                    stroke = mix(ColorRGBa.RED, ColorRGBa.YELLOW, 0.6)
                    strokeWeight = 4.0
                    fill = mix(ColorRGBa.RED, ColorRGBa.YELLOW, 0.6).opacify(0.2)
                    contour(
                        org.openrndr.shape.contour {
                            moveTo(convexPoints[0])
                            arcTo(convexRadius, convexRadius, 0.0,
                                largeArcFlag = false,
                                sweepFlag = true,
                                end = convexPoints[1]
                            )
                            arcTo(movingCirc.radius, movingCirc.radius, 0.0, isAngleReflex(a1), true, convexPoints[3])
                            arcTo(convexRadius, convexRadius, 0.0,
                                largeArcFlag = false,
                                sweepFlag = true,
                                end = convexPoints[2]
                            )
                            arcTo(leftCirc.radius, leftCirc.radius, 0.0, !isAngleReflex(a0), true, convexPoints[0])
                            close()
                        }
                    )
                    stroke = ColorRGBa.RED
                    convexPoints.forEach {
                        circler(Circle(it, 5.0))
                    }
                    fill = null
                }

                val concavePoints = mutableListOf<Vector2>()
                val concaveRadius = 80.0
                val tangentCircles = rightCirc.tangentCirclesConcave(movingCirc, concaveRadius)
                tangentCircles.forEach { circle ->
                    drawer.strokeWeight = 1.0
                    circler(circle)
                    val s0 = Segment(
                        rightCirc.center,
                        rightCirc.center - (rightCirc.center - circle.center) * rightCirc.radius
                    )
                    segment(s0)
                    concavePoints.addAll(s0.intersections(rightCirc).map {
                        it.position })

                    val s1 = Segment(
                        movingCirc.center,
                        movingCirc.center - (movingCirc.center - circle.center) * movingCirc.radius
                    )
                    segment(s1)
                    concavePoints.addAll(s1.intersections(movingCirc).map {
                        it.position
                    })
                }

                if (concavePoints.size == 4 && (!tangentCircles[0].overlap(tangentCircles[1]) || movingCirc.overlap(rightCirc))) {
                    val a0 = angle(rightCirc.center, concavePoints[0], concavePoints[2])
                    val a1 = angle(movingCirc.center, concavePoints[1], concavePoints[3])

                    stroke = mix(ColorRGBa.RED, ColorRGBa.YELLOW, 0.6)
                    strokeWeight = 4.0
                    fill = mix(ColorRGBa.RED, ColorRGBa.YELLOW, 0.6).opacify(0.2)
                    contour(
                        org.openrndr.shape.contour {
                            moveTo(concavePoints[0])
                            arcTo(concaveRadius, concaveRadius, 0.0,
                                largeArcFlag = false,
                                sweepFlag = true,
                                end = concavePoints[1]
                            )
                            arcTo(movingCirc.radius, movingCirc.radius, 0.0, !isAngleReflex(a1), false, concavePoints[3])
                            arcTo(concaveRadius, concaveRadius, 0.0,
                                largeArcFlag = false,
                                sweepFlag = true,
                                end = concavePoints[2]
                            )
                            arcTo(rightCirc.radius, rightCirc.radius, 0.0, isAngleReflex(a0), false, concavePoints[0])
                            close()
                        }
                    )
                    stroke = ColorRGBa.RED
                    concavePoints.forEach {
                        circler(Circle(it, 5.0))
                    }
                }

            }
        }
    }
}
