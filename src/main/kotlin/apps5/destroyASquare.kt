package apps5

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolated
import org.openrndr.extra.noise.Random
import org.openrndr.extra.shapes.grid

/**
 * A grid of incomplete squares. Uses the new signature of .grid() which I
 * developed for this sketch (taking two Double instead of two Int args)
 *
 * Based on
 * https://www.reddit.com/r/PlotterArt/comments/rwx7iq/genuary_day_5_destroy_a_square/
 * and
 * https://github.com/hapiel/Genuary-2022/blob/main/day05_destroy_a_square/sketch_day05_destroy_a_square.py
 */

fun main() = application {
  configure {
    width = 800
    height = 800
  }
  program {
    val squares = drawer.bounds.grid(
      40.0, 40.0, 80.0, 80.0, 10.0, 10.0
    ).flatten().map { rect ->
      val start = Random.int(0, 11) * 0.1
      val len = Random.int(1, 11) * 0.1
      rect.contour.sub(start, start + len)
    }

    extend {
      drawer.isolated {
        clear(ColorRGBa.WHITE)
        fill = null
        stroke = ColorRGBa.BLACK
        contours(squares)
      }
    }
  }
}
