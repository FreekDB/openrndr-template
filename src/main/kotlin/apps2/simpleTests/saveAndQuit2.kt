package apps2.simpleTests

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.Random
import org.openrndr.extra.noise.uniform
    
/**
 * id: f78b6749-fd3c-41c3-9a9c-079f2d3d7f2a
 * description: New sketch
 * tags: #new
 */    

/**
 * description: Second example for the forum that saves an image to disk and quits
 * tags: #autoquit
 */

fun main() = application {
    program {
        val screenshots = extend(Screenshots()) {
            async = false
            quitAfterScreenshot = true
        }
        extend {
            drawer.clear(ColorRGBa.WHITE)
            drawer.stroke = null
            drawer.circles {
                repeat(20) {
                    fill = if (it % 2 == 0) ColorRGBa.PINK else ColorRGBa.WHITE
                    circle(drawer.bounds.uniform(100.0), Random.int(2, 10) * 10.0)
                }
            }
            screenshots.trigger()
        }
    }
}
