package aBeLibs.svg

import aBeLibs.geometry.separated
import aBeLibs.math.map
import org.openrndr.extra.noise.Random
import org.openrndr.extra.noise.poissonDiskSampling
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.shape.*
import kotlin.math.pow

class Pattern {
    companion object {
        var stroke: Boolean = false
    }

    data class STRIPES(
        val expo: Double = 1.0,
        val density: Double = 1.0,
        val rotation: Double = 0.0
    )

    data class NOISE(
        val expo: Double = 1.0,
        val density: Double = 1.0,
        val rotation: Double = 0.0,
        val displacement: Double = 0.5
    )

    data class CIRCLES(
        val expo: Double = 1.0,
        val center: Vector2,
        val density: Double = 1.0
    )

    data class HAIR(
        val length: Double = 2.0,
        val zoom: Double = 1.0,
        val separation: Double = 1.0
    )

    data class PERP(
        val length: Double = 2.0,
        val separation: Double = 1.0
    )

    data class DOTS(
        val fillPercent: Double = 0.001,
        val minSize: Double = 5.0,
        val maxSize: Double = 20.0,
        val sizeSkew: Double = 1.0,
        val separation: Double = 5.0
    )
}

fun Composition.fill(outline: Shape, patternCfg: Any) {
    var pattern: Composition? = null
    val radius = outline.bounds.dimensions.length / 2
    val off = outline.bounds.center
    this.draw {
        when (patternCfg) {

            is Pattern.STRIPES -> {
                val num = (radius * patternCfg.density).toInt()
                val rot = patternCfg.rotation
                pattern = outline.addPattern {
                    lineSegments(List(num) {
                        val yNorm = (it / (num - 1.0)).pow(patternCfg.expo)
                        val x = ((it % 2) * 2 - 1.0) * radius
                        val y = (yNorm * 2 - 1) * radius
                        val start = Vector2(-x, y).rotate(rot) + off
                        val end = Vector2(x, y).rotate(rot) + off
                        LineSegment(start, end)
                    })
                }
            }

            is Pattern.NOISE -> {
                val num = (radius * patternCfg.density).toInt()
                val rot = patternCfg.rotation
                pattern = outline.addPattern {
                    contours(List(num) {
                        val yNorm = (it / (num - 1.0)).pow(patternCfg.expo)
                        val x = ((it % 2) * 2 - 1.0) * radius
                        val y = (yNorm * 2 - 1) * radius
                        val start = Vector2(-x, y).rotate(rot) + off
                        val end = Vector2(x, y).rotate(rot) + off
                        // TODO: go from start to end, shifting y with
                        //  noise(x, y)
                        val points = List(55) { Vector2(0.0) }
                        ShapeContour.fromPoints(points, false)
                    })
                }
            }

            is Pattern.CIRCLES -> {
                val num = (radius * patternCfg.density).toInt()
                pattern = outline.addPattern {
                    contours(List(num) {
                        val t = (it / (num - 1.0)).pow(patternCfg.expo)
                        Circle(patternCfg.center + off, 1 + 2 * radius * t)
                            .contour.open
                    })
                }
            }

            is Pattern.HAIR -> {
                val positions = poissonDiskSampling(
                    outline.bounds.width,
                    outline.bounds.height,
                    patternCfg.separation, 20
                ) { _: Double, _: Double, p: Vector2 ->
                    outline.contains(p + outline.bounds.corner)
                }

                pattern = outline.addPattern {
                    lineSegments(positions.map {
                        val offset = Polar(
                            360 * Random.simplex(
                                it * patternCfg.zoom +
                                        outline.bounds.corner * 0.1
                            ),
                            patternCfg.length / 2.0
                        ).cartesian
                        val p = it + outline.bounds.corner
                        LineSegment(p - offset, p + offset)
                    })
                }
            }

            is Pattern.PERP -> {
                val positions = poissonDiskSampling(
                    outline.bounds.width,
                    outline.bounds.height,
                    patternCfg.separation, 20
                ) { _: Double, _: Double, p: Vector2 ->
                    outline.contains(p + outline.bounds.corner)
                }

                pattern = outline.addPattern {
                    val contour = outline.contours.first()
                    lineSegments(positions.map {
                        val p = it + outline.bounds.corner
                        val offset = (contour.nearest(p).position - p)
                            .normalized * patternCfg.length / 2.0
                        LineSegment(p - offset, p + offset)
                    })
                }
            }

            is Pattern.DOTS -> {
                // openrndr 0.4 doesn't provide .area yet
                //val count = 2 + (outline.area * patternCfg.fillPercent).toInt()
                val count = 2 + (outline.bounds.width * outline.bounds.height
                        * 0.79 * patternCfg.fillPercent).toInt()
                var positions = (Random.ring2d(0.0, radius, count) as
                        List<Vector2>).map {
                    val rad = Random.double0().pow(patternCfg.sizeSkew).map(
                        patternCfg.minSize, patternCfg.maxSize
                    )
                    Circle(it + off, rad)
                }
                for (i in 0..100) {
                    positions = positions.separated(patternCfg.separation,
                        outline.contours[0])
                }
                pattern = outline.addPattern {
                    contours(positions.map { it.contour.open })
                }
            }
        }
        pattern?.apply {
            composition(this)
        }
        if (Pattern.stroke) {
            shape(outline)
        }
    }
}

/**
 * Takes a function that draws a pattern and discards everything
 * that is outside the "cookiecutter" Shape.
 *
 * @return A new composition including only the intersecting parts
 */
fun Shape.addPattern(pattern: CompositionDrawer.() -> Unit): Composition {
    val cutter = this
    // Clip pattern using the cutter shape
    return drawComposition {
        pattern()
        clipMode = ClipMode.INTERSECT
        shape(cutter)
    }
}
