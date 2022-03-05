package apps.simpleTests

import org.openrndr.application

/**
 * id: d3a45c54-daa6-4f15-b812-2db6c3df2ddf
 * description: New sketch
 * tags: #new
 */

fun main() = application {
    program {
        mouse.dragged.listen {
            println(it.modifiers)
        }
    }
}
