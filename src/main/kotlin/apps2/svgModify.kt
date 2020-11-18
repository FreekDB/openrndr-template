package apps2

import geometry.bend
import geometry.split
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.dialogs.saveFileDialog
import org.openrndr.shape.LineSegment
import org.openrndr.shape.drawComposition
import org.openrndr.svg.loadSVG
import org.openrndr.svg.writeSVG

fun main() = application {
    configure {
        width = 1920
        height = 1080
    }
    program {
        val shapes = loadSVG(
                // "/home/funpro/src/OR/openrndr-template/print/exhibition-growth-2.svg"
                "/tmp/map.svg")
                .findShapes().map { it.shape }

        val heart = loadSVG("/tmp/heart.svg")
                .findShapes().map { it.shape }

        //val knife = LineSegment(870.0, 0.0, 870.0 + 1200 * 0.4, 1200 * 1.0)
//        val knife = LineSegment(-800.0, -1000.0, 1200.0, 1000.0)
//                .contour.sampleEquidistant(50)

        val hearts = heart.map {
            it.contours
        }.flatten()
        val heart1 = hearts[0].sampleEquidistant(200)
        val heart2 = hearts[1].sampleEquidistant(200)

        val cutLines = shapes.map {
            it.contours
        }.flatten().map { it.split(heart1) }.flatten().bend(heart1)

        val cutLinesAgain = cutLines.map { it.split(heart2) }
                .flatten().bend(heart2)

        val svg = drawComposition {
            fill = null
            stroke = ColorRGBa.BLACK
            contours(cutLinesAgain)
//            stroke = ColorRGBa.RED
//            contour(knife)
        }
        saveFileDialog(supportedExtensions = listOf("svg")) {
            it.writeText(writeSVG(svg))
        }

        extend {
        }
    }
}

