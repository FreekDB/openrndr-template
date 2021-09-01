package apps.simpleTests

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.extensions.Screenshots
import org.openrndr.shape.Circle

fun main() {
    application {
        program {
            val multisample = renderTarget(width, height, multisample = BufferMultisample.SampleCount(8)) {
                colorBuffer()
                depthBuffer()
            }
            val resolved = colorBuffer(width, height)

            extend(Screenshots())
            extend {
                drawer.isolatedWithTarget(multisample) {
                    clear(ColorRGBa.BLACK)

                    this.drawCircle(ColorRGBa.WHITE, null, 80.0, listOf(0.2, 0.4))
                    this.drawCircle(null, ColorRGBa.WHITE, 100.0, listOf(0.2, 0.4))
                }
                multisample.colorBuffer(0).copyTo(resolved)
                drawer.image(resolved)

                drawer.drawCircle(ColorRGBa.WHITE, null, 80.0, listOf(0.6, 0.8))
                drawer.drawCircle(null, ColorRGBa.WHITE, 100.0, listOf(0.6, 0.8))
            }
        }
    }
}

private fun Drawer.drawCircle(fill: ColorRGBa?, stroke: ColorRGBa?, radius: Double, pos: List<Double>) {
    this.fill = fill
    this.stroke = stroke
    this.circle(this.bounds.dimensions * pos[0], radius)
    this.shape(Circle(this.bounds.dimensions * pos[1], radius).shape)
}
