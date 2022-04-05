package axi

import aBeLibs.geometry.circleish2
import aBeLibs.geometry.smoothed
import aBeLibs.geometry.toContours
import aBeLibs.lang.doubleRepeat
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.ColorBuffer
import org.openrndr.draw.Drawer
import org.openrndr.draw.loadImage
import org.openrndr.drawComposition
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.shapes.hobbyCurve
import org.openrndr.shape.ClipMode
import org.openrndr.shape.Rectangle
import org.openrndr.shape.Shape
import org.openrndr.shape.intersections
import org.openrndr.svg.writeSVG
import java.io.File
import kotlin.math.pow

/**
 * id: a4ebf148-f058-4146-b866-5f91bce30227
 * description: Page 52
 * tags: #axi
 */

/**
 * Feb 6, 2022 - Page 52
 * Draw with black pen over colored page.
 *
 * I scanned the page and use it here with BoofCV to obtain shapes.
 * Then I combine those shapes with generated circles to "draw behind"
 * detected shapes.
 *
 * I also tried applying handwriting to the resulting curves, but
 * it didn't look that great.
 *
 * The result, even without text, is quite bad. The black pen failed
 * to draw well and I had to do 4 passes. Still, it's like oil and water.
 *
 * I also didn't figure out why so many circles are missing. Like 90%?
 */

fun main() = application {
    configure {
        width = 1500
        height = 1200
    }
    program {
//        val handwritten = Handwritten().apply {
//            scale = 3.0
//        }
//
//        extend(handwritten)
//
//        File("data/2022-02-06.txt")
//            .readText()
//            .split("\n")
//            .forEach { l -> handwritten.add(l.trim(), Vector2.ZERO) }

        val img = loadImage("data/images/colored-book-page-small.jpg")
        val circleCount = 200
        val circles = List(circleCount) {
            val pc = it / circleCount.toDouble()
            val invPc = 1 - (1 - pc).pow(2.0)
            circleish2(
                drawer.bounds.position(0.38, 0.54),
                180.0 + invPc * 300, 30, 0.0,
                0.2 * pc, 0.5 + pc
            ).hobbyCurve().open
        }

        val boofeds = mutableListOf<Shape>()
        doubleRepeat(15, 0.4, 0.7) { threshold ->
            boofeds.addAll(img.toContours(threshold).filter {
                it.intersections(circles[circleCount / 2]).isNotEmpty()
            }.map {
                it.smoothed(2).shape
            })
        }

        val curves = circles.mapIndexed { i, circle ->
            drawComposition {
                fill = null
                stroke = ColorRGBa.BLACK.opacify(0.7)
                contour(circle)
                clipMode = ClipMode.INTERSECT
                shape(boofeds[i * boofeds.size / circles.size])
            }
        }

//        val curvesContours = curves.flatMap { composition ->
//            composition.findShapes().flatMap { shapeNode ->
//                shapeNode.shape.contours
//            }
//        }.filter { it.length > 35.0 }
//
//        val textSVG = CompositionDrawer()
//        textSVG.fill = null
//        textSVG.stroke = ColorRGBa.BLACK
//        handwritten.drawToSVG(textSVG, curvesContours)

        val svg = drawComposition {
            curves.forEach {
                composition(it)
            }
            rectangle(drawer.bounds)
        }

        File("/tmp/a.svg").writeText(writeSVG(svg))

        extend(Screenshots())
        extend {
            drawer.image(img, drawer.bounds)
            //drawer.contours(curvesContours)
            //drawer.composition(textSVG.composition)
        }
    }
}

private fun Drawer.image(img: ColorBuffer, bounds: Rectangle) {
    this.image(img, bounds.corner, bounds.width, bounds.height)
}
