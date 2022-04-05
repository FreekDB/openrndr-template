package apps5

import org.openrndr.application
import org.openrndr.color.rgb
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.renderTarget
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.palette.PaletteStudio
import org.openrndr.extra.shapes.grid

/**
 * id: 1093deca-4348-467b-98a0-9b2ce037327c
 * description: Test palette sorting
 * tags: #sorting #colors
 */

fun main() = application {
    program {
        val drawLayer = renderTarget(width, height) {
            colorBuffer()
        }

        val palettes = PaletteStudio(
            sortBy = PaletteStudio.SortBy.DARKEST,
            collection = PaletteStudio.Collections.TWO
        )

        fun makeIt() {
            drawer.isolatedWithTarget(drawLayer) {
                clear(rgb(0.5))
                stroke = null
                bounds.grid(
                    palettes.colors.size, 12,
                    20.0, 20.0, 2.0, 8.0
                ).forEach { rectangles ->
                    palettes.randomPalette()
                    rectangles.forEachIndexed { x, rect ->
                        fill = palettes.colors[x]
                        rectangle(rect)
                    }
                }
            }
        }

        makeIt()

        extend(Screenshots())
        extend {
            drawer.image(drawLayer.colorBuffer(0))
        }
        keyboard.keyUp.listen {
            if (it.name == "n") {
                makeIt()
            }
        }
    }
}
