package apps2.city

import org.openrndr.MouseButton
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.Random
import org.openrndr.math.Vector2

/**
 * id: c4af1a35-ac42-46bf-87dd-b509f2110805
 * description: New sketch
 * tags: #new
 */

const val blobFeedsCityDistance = 100.0

fun main() = application {
    configure {
        width = 1024
        height = 1024
    }

    program {
        val cities = mutableListOf<City>()
        val blobs = mutableListOf<Blob>()

        /**
         * Populates `blobs` simulating the blobs obtained from a
         * Kinect camera
         */
        fun updateBlobs(t: Double) {
            blobs.clear()
            if (mouse.pressedButtons.contains(MouseButton.LEFT)) {
                blobs.add(Blob(mouse.position))
            }
            val numBlobs = (Random.simplex(t * 5, 13.3) * 3.0 + 3.0).toInt()
            for (i in 0 until numBlobs) {
                blobs.add(
                    Blob(
                        (Vector2(
                            Random.simplex(i * 27.73, t),
                            Random.simplex(t, i * 39.13)
                        ) * 0.5 + 0.5) * drawer.bounds.dimensions
                    )
                )
            }
            blobs.forEach { blob ->
                if (cities.all { city ->
                        city.position.distanceTo(blob.position) > blobFeedsCityDistance
                    }) {
                    cities.add(City(blob.position))
                }
            }
        }

        /**
         * Updates `cities`. They grow if blobs are near, shrink otherwise.
         * If they shrink too much, size is negative. Time to delete them.
         */
        fun updateCities() {
            cities.forEach { it.evolve(blobs) }
            cities.removeIf { it.size < 0.0 }
        }

        extend(Screenshots())
        extend {
            updateBlobs(seconds * 0.01)
            updateCities()
            drawer.run {
                clear(ColorRGBa.BLACK)

                stroke = ColorRGBa.RED
                fill = ColorRGBa.RED.opacify(0.2)
                circles(blobs.map { it.position }, 50.0)

                stroke = ColorRGBa.WHITE
                fill = ColorRGBa.WHITE.opacify(0.1)
                cities.forEach { circle(it.position, it.size) }
            }
        }
    }
}
