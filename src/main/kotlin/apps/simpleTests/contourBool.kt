package apps.simpleTests

import aBeLibs.kotlin.loopRepeat
import com.soywiz.korma.geom.shape.Shape2d
import com.soywiz.korma.geom.shape.getAllPoints
import com.soywiz.korma.geom.shape.ops.minus
import com.soywiz.korma.geom.shape.ops.plus
import com.soywiz.korma.geom.toPoints
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.LineJoin
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.shape.ShapeContour

//implementation("com.soywiz.korlibs.korma","korma-jvm","2.0.9")
//implementation("com.soywiz.korlibs.korma","korma-shape","2.0.9")

fun main() = application {
    program {
        var s1: Shape2d = Shape2d.Circle(width * 0.5, height * 0.5, 200.0, 60)
        loopRepeat (8, to = 360.0) { a ->
            val pos = Polar(a, 200.0).cartesian + drawer.bounds.center
            val cookieCutter: Shape2d = Shape2d.Circle(pos.x, pos.y, 70.0)
            s1 = cookieCutter - s1 // s1 xor cookieCutter
        }
        loopRepeat (8, to = 360.0) { a ->
            val pos = Polar(a + 22.5, 200.0).cartesian + drawer.bounds.center
            val cookieCutter: Shape2d = Shape2d.Circle(pos.x, pos.y, 30.0)
            s1 = cookieCutter + s1
        }
        val contour = ShapeContour.fromPoints(
            s1.getAllPoints().toPoints().map {
                Vector2(it.x, it.y)
            }, true
        )

        extend {
            drawer.clear(ColorRGBa.WHITE)
            drawer.fill = null
            drawer.stroke = ColorRGBa.BLACK
            drawer.lineJoin = LineJoin.BEVEL
            drawer.contour(contour)
        }
    }
}
