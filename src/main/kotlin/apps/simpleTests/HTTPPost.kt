package apps.simpleTests

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.color.rgb
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.time.Duration

/**
 * id: b45de914-1ccb-44e7-99bb-218b5a8db71d
 * description: New sketch
 * tags: #new
 */

fun main() = application {
    configure {
        height = 80
    }
    program {
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("http://colormind.io/api/"))
            .timeout(Duration.ofSeconds(10))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString("{\"model\":\"default\"}"))
            .build()
        val response = client.send(request, BodyHandlers.ofString())
        val palette = mutableListOf<ColorRGBa>()
        if (response.statusCode() == 200) {
            val nan = Regex("(\\D+)")
            palette.addAll(response.body()
                .split(nan)
                .filter { it.isNotBlank() }
                .map { it.toDouble() / 255.0 }
                .chunked(3) {
                    rgb(it[0], it[1], it[2])
                })
        }

        extend {
            palette.forEachIndexed { i, c ->
                val w = width.toDouble() / palette.size
                drawer.fill = c
                drawer.rectangle(i * w, 0.0, w, height * 1.0)
            }
        }
    }
}
