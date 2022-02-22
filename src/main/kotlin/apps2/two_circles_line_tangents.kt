package apps2

import aBeLibs.geometry.*
import aBeLibs.math.angle
import aBeLibs.math.isAngleReflex
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.color.mix
import org.openrndr.draw.DrawStyle
import org.openrndr.draw.LineCap
import org.openrndr.draw.LineJoin
import org.openrndr.extras.color.presets.LIGHT_STEEL_BLUE
import org.openrndr.math.Vector2
import org.openrndr.shape.Circle
import org.openrndr.shape.Segment
import org.openrndr.shape.contour

/**
 * Ported from
 * [github](https://github.com/hamoid/Fun-Programming/tree/master/processing/ideas/2016/07/two_circles_line_tangents)
 * then added concave and convex circle tangents.
 */

fun main() = application {
    program {
        val orange = mix(ColorRGBa.RED, ColorRGBa.YELLOW, 0.6)
        val styleBase = DrawStyle(
            stroke = ColorRGBa.GRAY,
            fill = null,
            lineJoin = LineJoin.BEVEL,
            lineCap = LineCap.ROUND
        )
        val styleLines = styleBase.copy(
            stroke = ColorRGBa.LIGHT_STEEL_BLUE
        )
        val styleResult = DrawStyle(
            stroke = orange,
            strokeWeight = 4.0,
            fill = orange.opacify(0.2)
        )
        val styleVerts = styleResult.copy(
            stroke = ColorRGBa.RED,
            fill = null
        )


        extend {
            drawer.run {
                clear(ColorRGBa.WHITE)

                val circleL = Circle(bounds.center - 200.0, 100.0)
                val circleR = Circle(bounds.center + 200.0, 100.0)
                val circleM = Circle(mouse.position, 50.0)

                drawStyle = styleBase
                circler(circleL)
                circler(circleR)
                circler(circleM)

                drawStyle = styleLines
                val convexPoints = mutableListOf<Vector2>()
                val convexRadius = 200.0
                circleL.tangentCirclesConvex(circleM, convexRadius)
                    .forEach { circle ->
                        circler(circle)
                        val s0 = Segment(
                            circleL.center,
                            circleL.center + (circleL.center - circle.center) * circleL.radius
                        )
                        segment(s0)
                        convexPoints.addAll(
                            s0.intersections(circleL).map { it.position })

                        val s1 = Segment(
                            circleM.center,
                            circleM.center + (circleM.center - circle.center) * circleM.radius
                        )
                        segment(s1)
                        convexPoints.addAll(
                            s1.intersections(circleM).map { it.position })
                    }

                if (convexPoints.size == 4) {
                    val a0 =
                        angle(circleL.center, convexPoints[0], convexPoints[2])
                    val a1 = angle(
                        circleM.center,
                        convexPoints[2],
                        convexPoints[2]
                    )

                    drawStyle = styleResult
                    contour(
                        contour {
                            moveTo(convexPoints[0])
                            arcTo(
                                convexRadius, convexRadius, 0.0,
                                largeArcFlag = false,
                                sweepFlag = true,
                                end = convexPoints[1]
                            )
                            arcTo(
                                circleM.radius,
                                circleM.radius,
                                0.0,
                                isAngleReflex(a1),
                                true,
                                convexPoints[3]
                            )
                            arcTo(
                                convexRadius, convexRadius, 0.0,
                                largeArcFlag = false,
                                sweepFlag = true,
                                end = convexPoints[2]
                            )
                            arcTo(
                                circleL.radius,
                                circleL.radius,
                                0.0,
                                !isAngleReflex(a0),
                                true,
                                convexPoints[0]
                            )
                            close()
                        }
                    )
                    drawStyle = styleVerts
                    convexPoints.forEach {
                        circler(Circle(it, 5.0))
                    }
                }

                drawStyle = styleLines
                val concavePoints = mutableListOf<Vector2>()
                val concaveRadius = 80.0
                val tangentCircles =
                    circleR.tangentCirclesConcave(circleM, concaveRadius)
                tangentCircles.forEach { circle ->
                    circler(circle)
                    val s0 = Segment(
                        circleR.center,
                        circleR.center - (circleR.center - circle.center) * circleR.radius
                    )
                    segment(s0)
                    concavePoints.addAll(s0.intersections(circleR).map {
                        it.position
                    })

                    val s1 = Segment(
                        circleM.center,
                        circleM.center - (circleM.center - circle.center) * circleM.radius
                    )
                    segment(s1)
                    concavePoints.addAll(s1.intersections(circleM).map {
                        it.position
                    })
                }

                if (concavePoints.size == 4 && (!tangentCircles[0].overlap(
                        tangentCircles[1]
                    ) || circleM.overlap(circleR))
                ) {
                    val a0 = angle(
                        circleR.center,
                        concavePoints[0],
                        concavePoints[2]
                    )
                    val a1 = angle(
                        circleM.center,
                        concavePoints[1],
                        concavePoints[3]
                    )

                    drawStyle = styleResult
                    contour(
                        contour {
                            moveTo(concavePoints[0])
                            arcTo(
                                concaveRadius, concaveRadius, 0.0,
                                largeArcFlag = false,
                                sweepFlag = true,
                                end = concavePoints[1]
                            )
                            arcTo(
                                circleM.radius,
                                circleM.radius,
                                0.0,
                                !isAngleReflex(a1),
                                false,
                                concavePoints[3]
                            )
                            arcTo(
                                concaveRadius, concaveRadius, 0.0,
                                largeArcFlag = false,
                                sweepFlag = true,
                                end = concavePoints[2]
                            )
                            arcTo(
                                circleR.radius,
                                circleR.radius,
                                0.0,
                                isAngleReflex(a0),
                                false,
                                concavePoints[0]
                            )
                            close()
                        }
                    )
                    drawStyle = styleVerts
                    concavePoints.forEach {
                        circler(Circle(it, 5.0))
                    }
                }

            }
        }
    }
}
