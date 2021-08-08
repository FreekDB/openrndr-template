package apps2.city

import org.openrndr.applicationSynchronous
import org.openrndr.color.ColorRGBa
import org.openrndr.color.rgb
import org.openrndr.dialogs.saveFileDialog
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.Random
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle

// DONE: export collada mesh with UV maps
// TODO: decide: grid or no grid?
// TODO: deform grid and grid contents
// TODO: be able to paint in the grid (city center, radial roads)

fun main() = applicationSynchronous {
    configure {
        width = 1024
        height = 1024
    }
    program {
        val tiles = mutableListOf<Tile>()
        val gridSize = 20
        List(gridSize * gridSize) {
            Vector2(
                (it % gridSize) - gridSize * 0.5,
                (it / gridSize) - gridSize * 0.5
            )
        }.filter {
            it.length < (gridSize / 2 - 1)
        }.sortedBy {
            it.length
        }.forEach {
            val t = Tile(it)
            t.build()
            tiles.add(t)
        }

        fun saveCollada() {
            val collada = Collada()


            // ------ STREETS --------
            val streets = tiles.flatMap { it.streets }
            val streetWidths = tiles.flatMap { it.streetWidths }
            collada.addLineSegments("streets", streets, streetWidths)

            // ------- HOUSES -------
            val houses = tiles.flatMap { it.houses }
            val houseWidths = tiles.flatMap { it.houseWidths }

            // These are used for revealing the houses one by one. They will control
            // the .alpha of the quad.
            val uv0 = houses.map {
                Rectangle.fromCenter(Random.vector2(0.0, 1.0), 0.0, 0.0)
            }
            // These are used for texture mapping.
            val textureCols = 5
            val textureRows = 5
            val uv1 = houses.map {
                Rectangle(
                    Random.int0(textureCols) / textureCols.toDouble(),
                    Random.int0(textureRows) / textureRows.toDouble(),
                    1.0 / textureCols,
                    1.0 / textureRows
                )
            }
            collada.addLineSegments("houses", houses, houseWidths, uv0, uv1)

            saveFileDialog(supportedExtensions = listOf("dae")) { f ->
                collada.save(f)
                println("saved to $f")
            }
        }

        extend(Screenshots())
        extend {
            drawer.run {
                clear(ColorRGBa.WHITE)
                stroke = rgb(0.5)
                translate(bounds.center)
                tiles.forEach { it.draw(this) }
            }
        }

        keyboard.keyDown.listen { keyEvent ->
            if (keyEvent.name == "s") {
                saveCollada()
            }
        }
    }
}
