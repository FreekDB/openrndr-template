
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extras.camera.OrbitalCamera
import org.openrndr.extras.camera.OrbitalControls
import org.openrndr.math.Vector3

fun main() = application {
    configure {
        width = 800
        height = 800

    }

    program {
        val camera = OrbitalCamera(
            Vector3(0.0, 0.0, 200.0),
            Vector3.ONE * (width / 2.0),
            45.0,
            1.0,
            700.0
        )
        val controls = OrbitalControls(camera, keySpeed = 10.0)

        //camera.dampingFactor = 0.0

        extend(camera)
        extend(controls)

        extend {
            drawer.clear(ColorRGBa.PINK)
            drawer.stroke = ColorRGBa.BLACK
            drawer.strokeWeight = 20.0
            drawer.lineSegment(
                Vector3(350.0, 100.0, 0.0),
                Vector3(550.0, 700.0, 500.0)
            )

        }
    }
}