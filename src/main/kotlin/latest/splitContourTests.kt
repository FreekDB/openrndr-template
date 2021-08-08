package latest


import org.openrndr.color.ColorRGBa
import org.openrndr.color.hsl
import org.openrndr.draw.isolated
import org.openrndr.math.IntVector2
import org.openrndr.math.Vector2
import org.openrndr.shape.Circle
import org.openrndr.shape.Rectangle
import org.openrndr.shape.Segment
import org.openrndr.shape.contour

fun main() = applicationSynchronous {
    configure {
        width = 1500
        height = 600
    }
    program {
        val b = Rectangle.fromCenter(Vector2.ZERO, 300.0)
        val c = b.center
        val targets = listOf(
            Circle(c, 100.0).contour,
            Circle(c, 100.0).contour.sub(0.5, 1.4)
        )
        val knives = listOf(
            contour {
                moveTo(b.position(0.1, 0.1))
                curveTo(
                    b.position(0.9, 0.1),
                    b.position(0.1, 0.9),
                    b.position(0.9, 0.9)
                )
            },
            Circle(c + 20.0, 120.0).contour,
            Rectangle.fromCenter(c, 180.0, 180.0).contour,
            Segment(c, c - Vector2(120.0, 0.0)).contour,
            Segment(c, c - Vector2(120.0)).contour
        )
        extend {
            drawer.clear(ColorRGBa.WHITE)
            knives.forEachIndexed { x, knife ->
                targets.forEachIndexed { y, target ->
                    drawer.isolated {
                        translate(b.dimensions / 2.0 +
                                b.dimensions * IntVector2(x, y).vector2)
                        fill = ColorRGBa.BLACK.opacify(0.1)
                        stroke = ColorRGBa.BLACK.opacify(0.2)
                        contour(target)
                        contour(knife)

                        val parts = target.split(knife)
                        fill = ColorRGBa.BLACK.opacify(0.5)
                        text("${parts.size} parts", 10.0, 5.0)
                        fill = null
                        strokeWeight = 4.0
                        parts.forEachIndexed { i, it ->
                            stroke = hsl(i * 200.0, 1.0, 0.5).toRGBa()
                            contour(it)
                            circle(it.position(0.0), 10.0)
                            circle(it.position(1.0), 20.0)
                        }
                    }
                }
            }
        }
    }
}
