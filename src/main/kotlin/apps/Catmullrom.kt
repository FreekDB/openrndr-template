package apps

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extensions.Screenshots
import org.openrndr.math.CatmullRomChain2
import org.openrndr.math.Polar
import org.openrndr.math.Vector2

fun main() = application {
    program {

        extend(Screenshots())
        extend {
            drawer.stroke = ColorRGBa.WHITE

            val p0 = Vector2(100.0, 240.0) + Polar(seconds * 100, 200.0).cartesian
            val p1 = Vector2(300.0, 240.0) + Polar(seconds * 100 + 180, 200.0).cartesian
            drawer.circles(listOf(p0, p1), 6.0)

            drawer.lineStrip(
                CatmullRomChain2(
                    listOf(
                        p0, // custom
                        Vector2(100.0, 240.0),
                        Vector2(150.0, 140.0),
                        Vector2(200.0, 240.0),
                        Vector2(250.0, 140.0),
                        Vector2(300.0, 240.0),
                        p1 // custom
                    )
                ).positions(100)
            )

            drawer.lineStrip(
                CatmullRomChain2(
                    listOf(
                        Vector2(99.0, 241.0), // mirrored
                        Vector2(100.0, 240.0),
                        Vector2(150.0, 140.0),
                        Vector2(200.0, 240.0),
                        Vector2(250.0, 140.0),
                        Vector2(300.0, 240.0),
                        Vector2(301.0, 241.0) // mirrored
                    )
                ).positions(100)
            )

        }
    }
}
