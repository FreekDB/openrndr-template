package apps

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.color.rgb
import org.openrndr.draw.Drawer
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.compositor.compose
import org.openrndr.extra.compositor.draw
import org.openrndr.extra.compositor.layer
import org.openrndr.extra.compositor.post
import org.openrndr.extra.fx.blur.ApproximateGaussianBlur
import org.openrndr.extra.noise.Random
import org.openrndr.math.Vector3

/**
 * id: de6cf8bf-1b61-4418-a134-f14613feebe1
 * description: Test `composite` by drawing 3 layers containing moving rings
 * of different sizes to simulate depth of field: the top and bottom layers
 * are blurry, the middle one is sharp.
 * tags: #new
 */

fun main() = application {
    configure {
        width = 900
        height = 900
    }

    program {
        data class Item(var pos: Vector3, val color: ColorRGBa) {
            fun draw(drawer: Drawer) {
                pos -= Vector3(pos.z * 3.0, 0.0, 0.0)
                if (pos.x < -100.0) {
                    pos = Vector3(width + 100.0, pos.y, pos.z)
                }
                drawer.fill = null
                drawer.stroke = color
                drawer.strokeWeight = 5.0 + 5.0 * pos.z
                drawer.circle(pos.xy, 10.0 + 90.0 * pos.z)
            }
        }

        val items = List(50) {
            val pos = Vector3(Random.double() * width, Random.double(0.1, 0.9) * height, Random.double())
            Item(pos, ColorRGBa.PINK.shade(Random.double(0.2, 0.9)))
        }.sortedBy { it.pos.z }

        val composite = compose {
            layer {
                draw {
                    drawer.stroke = null
                    items.filter { it.pos.z < 0.33 }.forEach {
                        it.draw(drawer)
                    }
                }
                post(ApproximateGaussianBlur()) {
                    window = 25
                    sigma = 5.00
                }
            }

            layer {
                draw {
                    drawer.stroke = null
                    items.filter { it.pos.z in 0.33..0.66 }.forEach {
                        it.draw(drawer)
                    }
                }
            }

            layer {
                draw {
                    drawer.stroke = null
                    items.filter { it.pos.z > 0.66 }.forEach {
                        it.draw(drawer)
                    }
                }
                post(ApproximateGaussianBlur()) {
                    window = 25
                    spread = 4.0
                    sigma = 5.00
                }
            }
        }

        extend(Screenshots())
        extend {
            drawer.clear(rgb(0.2))
            composite.draw(drawer)
        }

    }
}
