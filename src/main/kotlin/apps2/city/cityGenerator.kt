package apps2.city

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.color.rgb
import org.openrndr.draw.shadeStyle
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.glslify.preprocessGlslify
import org.openrndr.math.Vector2
import kotlin.math.absoluteValue

const val sz = 160.0
const val hf = sz / 2.0

fun main() = application {
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

        val glslSimplex = preprocessGlslify(
            "#pragma glslify: simplex = require(glsl-noise/simplex/2d)"
        )

        extend(Screenshots())
        extend {
            drawer.run {
                clear(ColorRGBa.WHITE)
                stroke = rgb(0.5)

                drawer.shadeStyle = shadeStyle {
                    vertexPreamble = glslSimplex
                    vertexTransform = """
                        vec4 pos = x_projectionMatrix * x_viewMatrix * x_modelMatrix * vec4(a_position, 0.0, 1.0);
                        x_position.x += 20.0 * simplex(pos.xy * 0.4);
                        x_position.y += 20.0 * simplex(pos.yx * 0.4 + 5.5);"""
                }

                val frm = (((frameCount / 10) % (tiles.size * 2)) - tiles.size).absoluteValue

                drawer.translate(drawer.bounds.center)
                for (i in 0 until frm) {
                    tiles[i].draw(this)
                }
            }
        }
    }
}
