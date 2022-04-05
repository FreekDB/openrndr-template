import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.DepthTestPass
import org.openrndr.draw.DrawPrimitive
import org.openrndr.draw.isolated
import org.openrndr.math.Vector2
import org.openrndr.math.Vector3
import org.openrndr.shape.Rectangle
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * id: 31f84d95-d50c-4751-a386-6404663e3701
 * description: New sketch
 * tags: #new
 */

fun main() = application {
    configure {
        width = 1920
        height = 1080
    }
    program {
        val rectHeight = 8.0
        val stripes = 25
        val colorOuter = ColorRGBa(0.5, 0.0, 0.0)
        val colorInner = ColorRGBa(0.8, 0.0, 0.0)

        extend {
            drawer.apply {
                depthWrite = true
                strokeWeight = 0.0
                stroke = ColorRGBa.TRANSPARENT
                depthTestPass = DepthTestPass.LESS_OR_EQUAL
                shadeStyle = null //gradient
                translate(0.0, 0.0, -600.0)
                perspective(90.0, 16.0 / 9.0, 1.0, -300.0)
            }

            for (numOuter in 1 until stripes - 1) {
                val tempo = sin(seconds * 0.5 + (numOuter / 23.5))
                val yTrans = (sin(2.0 * seconds + (numOuter / 23.5)) * 8) + numOuter * 20.0 - 240.0

                val apothemOuter = (stripes / 2 - abs(numOuter - (stripes / 2))) * 10.0
                val apothemInner = apothemOuter - 0.5
                val rectWidthOuter = apothemOuter / 0.866
                val rectWidthInner = apothemInner / 0.866
                val rectOuter = Rectangle.fromCenter(Vector2.ZERO, rectWidthOuter, rectHeight)
                val rectInner = Rectangle.fromCenter(Vector2.ZERO, rectWidthInner, rectHeight)

                for(num in 0 until 6) {
                    val offset = (PI / 3.0) * num
                    val rot = (360.0 / (2.0 * PI)) + (360.0 * (num / 6.0))

                    drawer.isolated {
                        translate(
                            sin(tempo + offset) * apothemOuter,
                            yTrans,
                            cos(tempo + offset) * apothemOuter
                        )
                        rotate(Vector3.UNIT_Y, tempo * rot)
                        fill = colorOuter
                        rectangle(rectOuter)
                    }

                    drawer.isolated {
                        translate(
                            sin(tempo + offset) * apothemInner,
                            yTrans,
                            cos(tempo + offset) * apothemInner
                        )
                        rotate(Vector3.UNIT_Y, tempo * rot)
                        fill = colorInner
                        rectangle(rectInner)
                    }
                }
            }
        }
    }
}
