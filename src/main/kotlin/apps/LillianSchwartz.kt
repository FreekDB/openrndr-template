package apps

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.extra.jumpfill.directionFieldFromBitmap
import org.openrndr.extra.jumpfill.distanceFieldFromBitmap
import shadestyles.PushAway

/**
 * Creative Code Jam June 20th, 2020
 * With Deniz - Based on https://vimeo.com/56480534
 */

fun main() = application {
    configure {
        width = 1024
        height = 1024
    }

    program {
        val distanceField = colorBuffer(width, height, type = ColorType.FLOAT32)
        val directionField = colorBuffer(width, height, type = ColorType.FLOAT32)
        val rt = renderTarget(width, height) {
            colorBuffer(type = ColorType.FLOAT32)
            depthBuffer()
        }.apply { clearDepth(0.0) }
        val result = colorBuffer(width, height)
        val pushAway = PushAway()

        extend {
            drawer.clear(ColorRGBa.BLACK)
            distanceFieldFromBitmap(rt.colorBuffer(0), result = distanceField)
            directionFieldFromBitmap(rt.colorBuffer(0), result = directionField)

            //pushAway.apply(arrayOf(rt.colorBuffer(0), distanceField, directionField), result)
            //result.copyTo(rt.colorBuffer(0))

            when {
                keyboard.pressedKeys.contains("left-shift") -> {
                    drawer.shadeStyle = shadeStyle {
                        fragmentTransform = """
                                float d = x_fill.r;
                                if (x_fill.g > 0.5) { 
                                    x_fill.rgb = vec3(1.0, 0.0, 0.0);
                                } else {
                                    x_fill.rgb = vec3(1.0 / (0.5 + 0.05 * d));
                                }
                            """
                    }
                    drawer.image(distanceField)
                }
                keyboard.pressedKeys.contains("left-alt") -> {
                    drawer.shadeStyle = shadeStyle {
                        fragmentTransform = """
                                float a = atan(x_fill.r, x_fill.g);
                                if (x_fill.b > 0.5) { 
                                    x_fill.rgb = vec3(cos(a)*0.5+0.5, 1.0, sin(a)*0.5+0.5);
                                } else {
                                    x_fill.rgb = vec3(cos(a)*0.5+0.5, 0.0, sin(a)*0.5+0.5);
                                }
                            """
                    }
                    drawer.image(directionField)
                }
                else -> {
                    drawer.image(rt.colorBuffer(0))
                }
            }
        }
        mouse.dragged.listen {
            drawer.isolatedWithTarget(rt) {
                stroke = ColorRGBa.WHITE
                strokeWeight = 2.0 //it.dragDisplacement.length
                lineSegment(it.position - it.dragDisplacement, it.position)
            }
        }
        keyboard.keyDown.listen {
            if (it.name == "delete") {
                drawer.isolatedWithTarget(rt) {
                    clear(ColorRGBa.BLACK)
                }
            }
        }
    }
}
