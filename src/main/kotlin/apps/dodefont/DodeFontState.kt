package dodefont

import org.openrndr.color.ColorRGBa
import org.openrndr.extras.camera.OrbitalCamera
import org.openrndr.extras.camera.OrbitalControls
import org.openrndr.math.Vector3

object TPState {
    var bgColor = ColorRGBa.GRAY.shade(0.250)
    val camera = OrbitalCamera(Vector3.UNIT_Z * 1000.0, Vector3.ZERO, 90.0, 0.1, 2000.0)
    val controls = OrbitalControls(camera, keySpeed = 10.0)

    fun rnd() {
        bgColor = ColorRGBa(Math.random(), Math.random(), Math.random())
    }

}