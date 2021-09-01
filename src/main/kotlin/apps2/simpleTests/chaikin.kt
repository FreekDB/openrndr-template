package apps2.simpleTests

import aBeLibs.extensions.TransRotScale
import org.openrndr.application

import org.openrndr.color.rgb
import org.openrndr.math.chaikinSmooth
import org.openrndr.shape.Rectangle
import org.openrndr.shape.ShapeContour


fun main() = application {
    program {
        val lowpoly = Rectangle.fromCenter(drawer.bounds.center, 200.0)
            .contour

        val smooth =
            ShapeContour.fromPoints(chaikinSmooth(lowpoly.segments.map {
                it.start
            }, 3, true, 0.15), true)

        smooth.segments.forEach {
            if (it.normal(0.0).length < 0.5) {
                println("err $it")
            }
        }
        extend(TransRotScale())
        extend {
            drawer.clear(rgb(0.7))
            drawer.fill = null
            drawer.strokeWeight = 0.3
            //drawer.contour(lowpoly)
            drawer.contour(smooth)
        }
    }
}
