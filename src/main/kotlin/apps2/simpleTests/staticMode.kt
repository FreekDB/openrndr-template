package apps2.simpleTests

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.noclear.NoClear
import org.openrndr.extra.noise.Random
import org.openrndr.extra.noise.uniform
    
/**
 * id: fa1f096c-9cee-4978-bf99-6ae92c66ab69
 * description: New sketch
 * tags: #new
 */    

/**
 * description: Attempt to provide a behavior similar to the "static mode" in Processing
 * tags: #staticmode
 */

fun main() = application {
    program {
        extend(NoClear()) {
            backdrop = {
                drawer.clear(ColorRGBa.WHITE)
                drawer.stroke = null
                drawer.circles {
                    repeat(20) {
                        fill = if (it % 2 == 0) ColorRGBa.PINK else ColorRGBa.WHITE
                        circle(drawer.bounds.uniform(100.0), Random.int(2, 10) * 10.0)
                    }
                }
            }
        }
    }
}
