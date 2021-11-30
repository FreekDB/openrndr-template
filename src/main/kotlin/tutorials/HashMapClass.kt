package tutorials

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.loadFont
import org.openrndr.extra.noise.uniform
import org.openrndr.math.Vector2
import org.openrndr.math.map
import org.openrndr.shape.IntRectangle
import java.io.File

/**
 * Port of HashMapClass.pde (Processing example)
 */

fun main() = application {
    program {
        val fonts = List(10) {
            loadFont("data/fonts/SourceCodePro-Regular.ttf", 4.0 + 5 * it)
        }

        // Make spawnArea larger so it's not too crowded with words
        val spawnArea = IntRectangle(-50, 0, width, height * 3).rectangle

        // Visible area is expanded to start drawing items about to enter
        val visibleArea = drawer.bounds.offsetEdges(30.0)

        class Word(val word: String, count: Int, val color: ColorRGBa) {
            // Pick initial random position inside spawnArea
            private var position = Vector2.uniform(spawnArea)

            // Calculate speed based on word count
            private var speed = Vector2(
                0.0, count.toDouble().map(
                    5.0, 25.0, 0.1, 5.0, true
                )
            )

            // Pick font (size) based on word count
            private var font = fonts[count.toDouble().map(
                5.0, 25.0, 0.0, fonts.size - 1.0, true
            ).toInt()]

            fun display() {
                // Move Word down
                position += speed

                // If too far down, bring back up
                if (position.y > spawnArea.height) {
                    position -= Vector2(0.0, spawnArea.height)
                }

                // If `position` inside `visibleArea`, draw it
                if (visibleArea.contains(position)) {
                    drawer.fill = color
                    drawer.fontMap = font
                    drawer.text(word, position)
                }
            }
        }

        // Create a `Map<String, Int>` with words and their counts
        val freqsDracula = File("data/texts/dracula.txt")
            .readText().lowercase().split(Regex("\\W+"))
            .groupingBy { it }.eachCount().filter { it.value >= 5 }

        val freqsFranken = File("data/texts/frankenstein.txt")
            .readText().lowercase().split(Regex("\\W+"))
            .groupingBy { it }.eachCount().filter { it.value >= 5 }

        // Make sure words no words appear in both Maps. Unique words only.
        val uniqueDracula = freqsDracula - freqsFranken.keys
        val uniqueFranken = freqsFranken - freqsDracula.keys

        // Finally create a List<Word> to be displayed
        val words = uniqueDracula.map { (word, count) ->
            Word(word, count, ColorRGBa.WHITE)
        } + uniqueFranken.map { (word, count) ->
            Word(word, count, ColorRGBa.BLACK)
        }

        configure {
            width = 640
            height = 360
        }

        extend {
            drawer.clear(ColorRGBa.GRAY)
            words.forEach { it.display() }
        }
    }
}