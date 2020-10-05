package color

import org.openrndr.color.ColorRGBa
import org.openrndr.draw.ColorBuffer
import org.openrndr.draw.ColorBufferShadow
import org.openrndr.draw.loadImage
import org.openrndr.extra.noise.Random
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.math.clamp
import java.io.File
import kotlin.math.min

class ColorProviderImage(private val path: String) : ColorProvider {
    @Transient
    private lateinit var shadow: ColorBufferShadow

    @Transient
    private var img: ColorBuffer? = null

    var center = Vector2.ZERO
    var imgPath = ""
        set(value) {
            field = value
            println("ColorProviderImage.imgPath set! Loading: $imgPath")
            img = loadImage(File(value))
            center = img!!.bounds.center
            radius = min(center.x, center.y)
            shadow = img!!.shadow
            shadow.download()
        }
    override var offset = 0.0
    var radius = 100.0

    override fun getColor(angle: Double): ColorRGBa {
        if (imgPath.isEmpty()) {
            reset()
        }
        img?.let {
            val p = center + Polar(angle + offset, radius).cartesian
            val q = p.clamp(Vector2.ZERO, it.bounds.dimensions)
            return shadow[q.x.toInt(), q.y.toInt()]
        }
        return ColorRGBa.PINK
    }

    override fun reset() {
        Random.seed = System.currentTimeMillis().toString()
        val images = File(path).walk().maxDepth(1).filter {
            val n = it.name.toLowerCase()
            n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".png")
        }.toList()
        imgPath = Random.pick(images).absolutePath
    }

    fun getImage(): ColorBuffer? {
        return img
    }

//    fun loadImage(path: File) {
//        img = org.openrndr.draw.loadImage(path)
//        center = img.bounds.center
//        radius = min(center.x, center.y)
//        shadow = img.shadow
//        shadow.download()
//    }

//    fun getPath(): String {
//        return imgPath
//    }
}
