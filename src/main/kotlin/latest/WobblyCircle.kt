package latest


import org.openrndr.color.ColorRGBa
import org.openrndr.color.rgb
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.shadestyles.radialGradient
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.shape.Segment
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.drawComposition
import kotlin.math.PI
import kotlin.math.tan

fun main() = applicationSynchronous {
    configure { width = 1100; height = 1100 }

    // Feature request: ShapeContour.resample(20) to create
    // a new ShapeContour with 20 equal length segments

    program {
        val colors = listOf(
            "19bcb9", "f0ece2", "5adad6", "ede9de",
            "d46a6a", "efece2", "f1c783", "efebe2"
        ).map(::rgb)
        val gradient = radialGradient(
            ColorRGBa.WHITE.shade(1.5),
            ColorRGBa.WHITE.shade(0.8),
            length = 0.7,
            exponent = 0.8
        )
        val design = drawComposition {
            translate(drawer.bounds.center)
            rotate(90.0)
            colors.forEachIndexed { it, color ->
                fill = color
                stroke = ColorRGBa.WHITE.opacify(0.6)
                val circle =
                    circleWithNSegments(
                        Vector2.ZERO,
                        width * 0.4 - it * width * 0.03,
                        34 - it * 4
                    ).wobble(6.0 + 3.0 * it)
                contour(circle)
                fill = colors[(it + 1) % colors.size]
                circles(circle.segments.map { it.start }, 5.0)
            }
        }

        extend(Screenshots())
        extend {
            drawer.shadeStyle = gradient
            drawer.rectangle(drawer.bounds)
            drawer.composition(design)
        }
    }
}

/**
 * Takes a ShapeContour and shifts the vertices along the normals,
 * odd ones out, even ones in, by the given offset amount.
 */
private fun ShapeContour.wobble(offset: Double) =
    ShapeContour(this.segments.mapIndexed { i, seg ->
        val odd = if (i % 2 == 1) 1.0 else -1.0
        val off1 = seg.normal(0.0) * offset * odd
        val off2 = seg.normal(1.0) * offset * odd
        Segment(
            seg.start + off1, seg.control[0] + off1,
            seg.control[1] - off2, seg.end - off2
        )
    }, closed)

/**
 * Creates a circular ShapeContour with the requested number of segments
 */
private fun circleWithNSegments(
    center: Vector2, radius: Double = 1.0, n: Int = 4
): ShapeContour {
    val theta = 360.0 / n
    val len = 4.0 / 3.0 * tan(PI / (2 * n))
    return ShapeContour(List(n) {
        val p0 = Polar(it * theta).cartesian
        val p1 = p0.perpendicular() * len
        val p3 = Polar((it + 1) * theta).cartesian
        val p2 = p3.perpendicular() * len
        val start = center + p0 * radius
        val end = center + p3 * radius
        Segment(
            start, start - p1 * radius,
            end + p2 * radius, end
        )
    }, true)
}
