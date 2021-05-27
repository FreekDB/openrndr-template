package latest

import aBeLibs.random.file
import org.openrndr.KEY_ENTER
import org.openrndr.application
import org.openrndr.boofcv.binding.resizeTo
import org.openrndr.color.ColorRGBa
import org.openrndr.color.rgba
import org.openrndr.dialogs.saveFileDialog
import org.openrndr.draw.*
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.Random
import org.openrndr.extra.noise.Random.unseeded
import org.openrndr.math.*
import org.openrndr.shape.Rectangle
import org.openrndr.shape.draw
import org.openrndr.shape.drawComposition
import org.openrndr.svg.saveToFile
import org.openrndr.utils.namedTimestamp
import kotlin.math.PI

/**
 * Loads a random image from a folder and lets the user
 * read the pixels from that image.
 */
data class ImageData(
    val path: String, val scanDist: Double = 8.0, val
    moveDist: Double = 3.0
) {
    /**
     * GPU buffer containing the image
     */
    lateinit var buff: ColorBuffer

    /**
     * pixels of the image available for reading
     */
    lateinit var pixels: ColorBufferShadow

    /**
     * Area equal to the image bounds minus `scanDist`
     */
    lateinit var safeArea: Rectangle

    fun loadNext() {
        unseeded {
            buff = loadImage(Random.file(path)).resizeTo(900, 900)
        }
        pixels = buff.shadow
        pixels.download()
        safeArea = buff.bounds.offsetEdges(scanDist * -2.0)
    }

    fun getColor(normPos: Vector2) = pixels[
            (normPos.x * buff.width).toInt().coerceIn(0 until buff.width),
            (normPos.y * buff.height).toInt().coerceIn(0 until buff.height)
    ]

    init {
        loadNext()
    }
}

fun main() = application {
    configure {
        width = 900
        height = 900
    }

    program {
        val img = ImageData("/home/funpro/Pictures/n1/Instagram/")
        val svg = drawComposition { }

        val rt = renderTarget(width, height) {
            colorBuffer()
        }.apply {
            clearColor(0, ColorRGBa.TRANSPARENT)
        }

        backgroundColor = ColorRGBa.WHITE

        // 1. Approach for moving forward
        // based on color similarity
        fun directionOfSimilarColor(pos: IntVector2, forward: Double): Double {
            val numSamples = (PI * img.scanDist).toInt()
            val samples = List(numSamples) {
                val angle = forward + it.toDouble().map(
                    0.0, numSamples - 1.0,
                    -30.0, 30.0
                )
                val next = pos + Polar(angle, img.scanDist).cartesian.toInt()
                Pair(angle, img.pixels[next.x, next.y])
            }
            val curr = img.pixels[pos.x, pos.y]
            return samples.minByOrNull {
                it.second.toVec3().squaredDistanceTo(curr.toVec3())
            }?.first!!
        }

        // 2. Approach for moving forward
        // hue change used for turning
        fun directionBentByHue(pos: IntVector2, forward: Double): Double {
            val a = img.pixels[pos.x, pos.y]
            val next = pos + Polar(forward, img.scanDist).cartesian.toInt()
            val b = img.pixels[next.x, next.y]
            return forward + (a.toHSVa().h - b.toHSVa().h)
        }

        extend(Screenshots())
        extend {
            if (keyboard.pressedKeys.contains("left-shift")) {
                drawer.image(img.buff)
            }
            drawer.image(rt.colorBuffer(0))
        }
        keyboard.keyDown.listen {
            if (it.key == KEY_ENTER) {
                val fileName = program.namedTimestamp("svg", "print")
                saveFileDialog(
                    suggestedFilename = fileName,
                    supportedExtensions = listOf("svg")
                ) { file ->
                    svg.saveToFile(file)
                }
            }
        }
        mouse.buttonDown.listen {
            rt.clearColor(0, ColorRGBa.TRANSPARENT)
            svg.clear()
            img.loadNext()
            val points = 720
            repeat(points) { point ->
                var pos = (img.safeArea.center +
                        Polar(
                            360.0 * point / points,
                            img.safeArea.width * 0.3
                        ).cartesian).toInt()
                var forward = img.pixels[pos.x, pos.y].toHSVa().h
                val positions = mutableListOf<Vector2>()

                for (i in 0..199) {
                    if (!img.safeArea.contains(pos.vector2)) {
                        break
                    }
                    pos.vector2.let {
                        if (positions.isEmpty() || it != positions.last()) {
                            positions.add(it)
                        }
                    }

                    //forward = directionOfSimilarColor(pos, forward)
                    forward = directionBentByHue(pos, forward)

                    pos += Polar(
                        forward jitter 2.0,
                        img.moveDist jitter 1.0
                    ).cartesian.toInt()
                }
                svg.draw {
                    lineStrip(positions)
                }
                drawer.isolatedWithTarget(rt) {
                    stroke = rgba(0.0, 0.0, 0.0, 0.7)
                    lineJoin = LineJoin.ROUND
                    lineStrip(positions)
                }
            }
        }
    }
}

private fun ColorRGBa.toVec3() = Vector3(r, g, b)

private infix fun Double.jitter(amount: Double) =
    this + Random.double(-amount, amount)
