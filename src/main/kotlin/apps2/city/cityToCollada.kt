package apps2.city

import aBeLibs.geometry.randomPoint
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.dialogs.saveFileDialog
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.Random
import org.openrndr.shape.LineSegment
import org.openrndr.shape.Rectangle

/**
 * id: 617df781-a748-4447-83ab-4ad5a1c1c24d
 * description: New sketch
 * tags: #new
 */

fun main() = application {
    configure {
        width = 1024
        height = 1024
    }
    program {
        extend(Screenshots())
        extend {
            drawer.run {
                clear(ColorRGBa.WHITE)
            }
        }
        keyboard.keyDown.listen { keyEvent ->
            if (keyEvent.name == "s") {

                val collada = Collada()

                // ------ STREETS --------
                val streets = List(15) {
                    LineSegment(
                        drawer.bounds.randomPoint(),
                        drawer.bounds.randomPoint()
                    )
                }
                val streetWidths = List(streets.size) { 2.0 }
                collada.addLineSegments("streets", streets, streetWidths)

                // ------- HOUSES -------
                val houses = streets.map {
                    val center = it.position(0.5)
                    LineSegment(
                        center + it.normal * 5.0,
                        center + it.normal * 15.0
                    )
                }
                val houseWidths = List(houses.size) {
                    Random.double(5.0, 30.0)
                }
                val uv0 = List(houses.size) {
                    Rectangle.fromCenter(Random.vector2(0.0, 1.0), 0.0, 0.0)
                }
                val uv1 = List(houses.size) {
                    Rectangle.fromCenter(
                        Random.vector2(0.0, 1.0),
                        Random.double(0.1, 0.2),
                        Random.double(0.1, 0.2)
                    )
                }
                collada.addLineSegments("houses", houses, houseWidths, uv0, uv1)

                saveFileDialog(supportedExtensions = listOf("dae")) { f ->
                    collada.save(f)
                    println("saved to $f")
                }
            }
        }
    }
}
