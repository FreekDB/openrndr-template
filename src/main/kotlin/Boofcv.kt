import boofcv.alg.filter.binary.BinaryImageOps
import boofcv.alg.filter.binary.GThresholdImageOps
import boofcv.alg.filter.binary.ThresholdImageOps
import boofcv.io.image.ConvertBufferedImage
import boofcv.io.image.UtilImageIO
import boofcv.struct.ConnectRule
import boofcv.struct.image.GrayF32
import boofcv.struct.image.GrayS32
import boofcv.struct.image.GrayU8
import georegression.struct.point.Point2D_I32
import org.openrndr.*
import org.openrndr.color.ColorRGBa
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.palette.PaletteStudio
import org.openrndr.extra.shadestyles.LinearGradient
import org.openrndr.math.Vector2
import org.openrndr.shape.ShapeContour
import random.pickWeighted
import kotlin.system.exitProcess


/**
 * Basic template
 */

fun main() = application {
    val extContours = mutableListOf<ShapeContour>()
    val intContours = mutableListOf<ShapeContour>()

    program {
        val image = UtilImageIO.loadImage("data/images/Particles01.jpg")
        val input = ConvertBufferedImage.convertFromSingle(image, null, GrayF32::class.java)
        val binary = GrayU8(input.width, input.height)
        val label = GrayS32(input.width, input.height)
        val threshold = GThresholdImageOps.computeOtsu(input, 0.0, 255.0)
        ThresholdImageOps.threshold(input, binary, threshold.toFloat(), true)
        var filtered = BinaryImageOps.erode8(binary, 1, null)
        filtered = BinaryImageOps.dilate8(filtered, 1, null)
        val contours = BinaryImageOps.contour(filtered, ConnectRule.EIGHT, label)

        contours.forEach {
            extContours.add(ShapeContour.fromPoints(it.external.map { p -> vector2(p) }, true))
            it.internal.forEach { internalContour ->
                intContours.add(ShapeContour.fromPoints(internalContour.map { p -> vector2(p) }, true))
            }
        }

        val palette = PaletteStudio(
            loadDefault = true,
            sortBy = PaletteStudio.SortBy.DARKEST,
            collection = PaletteStudio.Collections.TWO,
            colorCountConstraint = 0
        )
        palette.randomPalette()

        val gradient = LinearGradient(ColorRGBa.WHITE.shade(0.5), ColorRGBa.WHITE, Vector2.ZERO)
        gradient.exponent = 5.0

        val colorIndex = extContours.map {
            listOf(0, 1, 2, 3, 4).pickWeighted(palette.colors.mapIndexed { i, _ -> i * i + 1.0 })
        }

        extend(palette)
        extend(Screenshots())
        extend {

            drawer.run {
                clear(ColorRGBa.WHITE.shade(0.3))
                extContours.forEachIndexed { i, it ->
                    stroke = null
                    fill = palette.colors[colorIndex[i]]
                    gradient.rotation = i * 45.0
                    shadeStyle = gradient
                    contour(it)
                }
//                stroke = ColorRGBa.BLUE
//                contours(intContours)
            }
        }
        keyboard.keyDown.listen {
            when (it.key) {
                KEY_ESCAPE -> exitProcess(0)
                KEY_ENTER -> palette.randomize()
            }
        }
    }
}

fun vector2(p: Point2D_I32): Vector2 {
    return Vector2(p.x * 1.0, p.y * 1.0)
}
