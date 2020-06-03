package color

import org.openrndr.color.ColorRGBa
import org.openrndr.draw.ColorBuffer
import org.openrndr.draw.ColorBufferShadow
import org.openrndr.draw.loadImage
import org.openrndr.extra.noise.Random
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import java.io.File
import kotlin.math.min

class ColorProviderImage(private val path: String = "/home/funpro/Pictures/n1/Instagram/") : ColorProvider {
    private lateinit var center: Vector2
    private lateinit var shadow: ColorBufferShadow
    override var offset = 0.0

    init {
        reset()
    }

    override fun getColor(angle: Double): ColorRGBa {
        val p = center + Polar(
            (angle + offset) * 0.05,
            min(center.x, center.y) * 0.9
        ).cartesian
        return shadow[p.x.toInt(), p.y.toInt()]
    }

    override fun reset() {
        Random.seed = System.currentTimeMillis().toString()
        val img = loadRandomImage(path)
        center = img.bounds.center
        shadow = img.shadow
        shadow.download()
    }

    private fun loadRandomImage(path: String): ColorBuffer {
        val images = File(path).walk().maxDepth(1).filter {
            val n = it.name.toLowerCase()
            n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".png")
        }.toList()
        val chosen = Random.pick(images)
        println(chosen.name)
        return loadImage(chosen)
    }
}
