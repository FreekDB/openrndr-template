package apps.simpleTests

import org.openrndr.KEY_SPACEBAR
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.color.mix
import org.openrndr.draw.Drawer
import org.openrndr.draw.isolated
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.shadestyles.linearGradient
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.math.map
import org.openrndr.shape.SegmentJoin
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.offset

/**
 * id: 069381ef-2651-44f6-8b9d-23f61897a3d7
 * description: Testing recent changes to ShapeContour and .offset()
 * tags: #new
 */

// A class to store a contour together with two colors used for its gradient
class GradientContour(val contour: ShapeContour, percent: Double) {
    val colorA = mix(
        ColorRGBa.fromHex(0xF8B195),
        ColorRGBa.fromHex(0xF67280),
        percent
    )
    val colorB = mix(
        ColorRGBa.fromHex(0xC06C84),
        ColorRGBa.fromHex(0x6C5B7B),
        percent
    )
}

// A collection of visually nested GradientContours created using .offset()
class Blob(private val pos: Vector2) {
    private val curves = mutableListOf<GradientContour>()

    init {
        // Make the core shape
        curves.clear()
        curves.add(GradientContour(
            ShapeContour.fromPoints(
                List(5) {
                    Math.random() * 360
                }.sorted().map { angle ->
                    Polar(angle, 50.0).cartesian
                }, true
            ), 0.0
        )
        )

        // Make offset shapes, each based on the previous one
        for (i in 0..6) {
            curves.add(
                GradientContour(
                    curves[i].contour.offset(-4.0 - i * 6.0, SegmentJoin.BEVEL),
                    i.toDouble().map(-1.0, 5.0, 0.0, 1.0)
                )
            )
        }
    }

    fun draw(drawer: Drawer) {
        drawer.isolated {
            drawer.translate(pos)
            // Draw first larger shapes, then smaller ones
            for (i in curves.size - 1 downTo 0) {
                val c = curves[i]
                drawer.stroke = ColorRGBa.BLACK.opacify(0.1)
                drawer.shadeStyle = linearGradient(c.colorA, c.colorB, rotation = i * 185.0)
                drawer.contour(c.contour)
            }
        }
    }

}

fun main() = application {
    configure {
        width = 900
        height = 900
        //hideWindowDecorations = true
    }

    program {
        val blobs = mutableListOf<Blob>()

        fun populate() {
            blobs.clear()
            blobs.add(Blob(Vector2(width * 0.27, height * 0.27)))
            blobs.add(Blob(Vector2(width * 0.73, height * 0.27)))
            blobs.add(Blob(Vector2(width * 0.27, height * 0.73)))
            blobs.add(Blob(Vector2(width * 0.73, height * 0.73)))
        }

        populate()

        extend(Screenshots()) {
            key = "s"
        }

        extend {
            drawer.clear(ColorRGBa.fromHex(0x355C7D))
            blobs.forEach { it.draw(drawer) }
        }

        keyboard.keyDown.listen {
            when (it.key) {
                KEY_SPACEBAR -> populate()
            }
        }
    }
}
