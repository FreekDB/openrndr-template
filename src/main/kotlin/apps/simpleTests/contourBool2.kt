package apps.simpleTests

import aBeLibs.kotlin.loopRepeat
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolated
import org.openrndr.math.Polar
import org.openrndr.shape.Circle
import org.openrndr.shape.Shape
import org.openrndr.shape.compound

fun main() = application {
    program {
        var shp = Circle(drawer.bounds.center, 200.0).shape
        loopRepeat(8, to = 360.0) { a ->
            shp -= Circle(Polar(a * 1.0, 200.0).cartesian + drawer.bounds.center, 70.0).shape
        }
        loopRepeat(8, to = 360.0) { a ->
            shp += Circle(Polar(a * 1.0 + 22.5, 200.0).cartesian + drawer.bounds.center, 30.0).shape
        }
        shp = shp intersection Circle(width * 0.5, height * 0.5, 210.0).shape

        extend {
            drawer.isolated {
                clear(ColorRGBa.WHITE)
                fill = ColorRGBa.PINK
                stroke = ColorRGBa.PINK.shade(0.4)
                strokeWeight = 2.0
                shape(shp)
            }
        }
    }
}

private infix fun Shape.intersection(other: Shape): Shape {
    return compound {
        intersection {
            shape(this@intersection)
            shape(other)
        }
    }[0]
}

private operator fun Shape.minus(other: Shape): Shape {
    return compound {
        difference {
            shape(this@minus)
            shape(other)
        }
    }[0]
}

private operator fun Shape.plus(other: Shape): Shape {
    return compound {
        union {
            shape(this@plus)
            shape(other)
        }
    }[0]
}
