package apps5

import com.soywiz.korma.random.randomWithWeights
import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.renderTarget
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.Random
import org.openrndr.extra.noise.gradientPerturbFractal
import org.openrndr.extra.noise.poissonDiskSampling
import org.openrndr.extra.noise.random
import org.openrndr.extra.palette.PaletteStudio
import org.openrndr.extra.shadestyles.LinearGradient
import org.openrndr.extra.shadestyles.RadialGradient
import org.openrndr.extra.triangulation.Delaunay
import org.openrndr.math.Vector2
import org.openrndr.shape.Circle
import org.openrndr.shape.ShapeContour
import kotlin.system.exitProcess

/**
 * A combination of voronoi01.kt and gridOfCircles.kt
 */

fun main() = application {
    configure {
        width = 800
        height = 800
    }

    program {
        val layer = renderTarget(width, height) {
            colorBuffer()
            depthBuffer()
        }
        val palettes =
            PaletteStudio(collection = PaletteStudio.Collections.THREE)

        fun makeIt() {
            val colorWeights = List(palettes.colors.size) {
                if (it > 0) 1.0
                else 20.0
            }
            drawer.isolatedWithTarget(layer) {

                val circle = Circle(drawer.bounds.center, width * 0.32)
                val points = poissonDiskSampling(
                    width * 1.0, height * 1.0, 15.0
                ) { _, _, v ->
                    val perturb = gradientPerturbFractal(
                        seconds.toInt(),
                        position = v * 0.008
                    )
                    Random.simplex(perturb) < Random.double(-1.0, 0.0)
                }
                val delaunay = Delaunay.from(
                    points + circle.contour.equidistantPositions(40)
                )
                val voronoi = delaunay.voronoi(drawer.bounds.scale(0.8))
                val contours = voronoi.cellsPolygons()
                val centers = contours.map { it.centroid() }
//                val centers = contours.map { contour ->
//                    contour.segments.map { it.start }.reduce { sum, element ->
//                        sum + element
//                    } / contour.segments.size.toDouble()
//                }

                val circles = centers.mapIndexed { i, c ->
                    Circle(c, contours[i].nearest(c).position.distanceTo(c))
                }

                val gradient0 = LinearGradient(
                    ColorRGBa.WHITE.opacify(0.1),
                    ColorRGBa.BLACK.opacify(0.1),
                    exponent = 8.0
                )
                val gradient1 = RadialGradient(
                    ColorRGBa.WHITE,
                    ColorRGBa.WHITE,
                    exponent = 4.0,
                    offset = Vector2(0.1, 0.1)
                )
                val gradient2 = RadialGradient(
                    ColorRGBa.WHITE,
                    ColorRGBa.WHITE,
                    exponent = 6.0,
                    offset = Vector2(-0.1, -0.1)
                )

                clear(palettes.background)

                stroke = ColorRGBa.WHITE.opacify(0.1)
                shadeStyle = gradient0
                contours.forEach {
                    gradient0.rotation = random(0.0, 360.0)
                    contour(it)
                }

                stroke = null
                circles.forEach { c ->
                    if (Random.bool(1.0)) {
                        val color =
                            palettes.colors.randomWithWeights(colorWeights)
                                .shade(random(0.8, 1.05))

                        gradient1.color0 = color
                        gradient1.color1 = color.shade(random(0.4, 0.6))
                        shadeStyle = gradient1
                        circle(c)

                        gradient2.color0 = color.shade(random(1.0, 1.2))
                        gradient2.color1 = color.shade(random(0.25, 0.35))
                        shadeStyle = gradient2
                        circle(c.scaledBy(0.5, 2.0, 2.0))
                    }
                }
            }
        }

        makeIt()

        extend(Screenshots())
        extend {
            drawer.clear(ColorRGBa.WHITE.shade(0.4))
            drawer.image(layer.colorBuffer(0))
        }
        keyboard.keyUp.listen {
            if (it.key == KEY_ESCAPE) {
                exitProcess(0)
            }
            when (it.name) {
                "n" -> makeIt()
                "p" -> {
                    palettes.randomPalette()
                    makeIt()
                }
                "s" -> {
                    palettes.randomize()
                    makeIt()
                }
            }
        }
    }
}

/**
 * For straight-segments-ShapeContour only
 */
private fun ShapeContour.area(): Double {
    val points = segments.map { it.start }
    var area = 0.0
    for (i in 0 until points.size - 1) {
        area += points[i].x * points[i + 1].y -
                points[i + 1].x * points[i].y
    }
    area += points[points.size - 1].x * points[0].y -
            points[0].x * points[points.size - 1].y
    return area * 0.5
}

/**
 * For straight-segments-convex-ShapeContour only
 */
private fun ShapeContour.centroid(): Vector2 {
    val points = segments.map { it.start }
    var centroid2D = Vector2.ZERO
    for (i in 0 until points.size - 1) {
        centroid2D += (points[i] + points[i + 1]) *
                (points[i].x * points[i + 1].y - points[i + 1].x * points[i].y)
    }
    centroid2D += (points[points.size - 1] + points[0]) *
            (points[points.size - 1].x * points[0].y - points[0].x * points[points.size - 1].y)

    return centroid2D / (area() * 6.0)
}
