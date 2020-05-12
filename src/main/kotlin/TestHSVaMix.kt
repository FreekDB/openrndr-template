import org.openrndr.application
import org.openrndr.color.ColorHSVa

fun main() = application {
    configure {
        width = 1500
        height = 800
    }

    program {
        val left = ColorHSVa(20.0, 1.0, 1.0)
        val right = ColorHSVa(340.0, 1.0, 1.0)
        val middle = left.mix(right, 0.5)
        extend {
            drawer.fill = left.toRGBa()
            drawer.rectangle(50.0, 50.0, 40.0, 200.0)

            drawer.fill = middle.toRGBa()
            drawer.rectangle(100.0, 50.0, 40.0, 200.0)

            drawer.fill = right.toRGBa()
            drawer.rectangle(150.0, 50.0, 40.0, 200.0)
        }
    }
}
