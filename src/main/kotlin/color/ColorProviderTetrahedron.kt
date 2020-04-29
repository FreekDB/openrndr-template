package color

import org.openrndr.color.ColorLABa
import org.openrndr.color.ColorRGBa
import org.openrndr.math.Vector3
import org.openrndr.math.mix

class ColorProviderTetrahedron(private val colors: List<ColorLABa>, val name: String) {

    constructor(a: String, b: String, c: String, d: String, name: String) : this(
        listOf(
            ColorRGBa.fromHex(a).toLABa(),
            ColorRGBa.fromHex(b).toLABa(),
            ColorRGBa.fromHex(c).toLABa(),
            ColorRGBa.fromHex(d).toLABa()
        ), name
    )

    private fun mix(left: ColorLABa, right: ColorLABa, x: Double): ColorLABa {
        val sx = x.coerceIn(0.0, 1.0)
        return ColorLABa(
            mix(left.l, right.l, sx),
            mix(left.a, right.a, sx),
            mix(left.b, right.b, sx),
            mix(left.alpha, right.alpha, sx)
        )
    }

    private fun mix(left: ColorRGBa, right: ColorRGBa, x: Double): ColorRGBa {
        val sx = x.coerceIn(0.0, 1.0)
        return ColorRGBa(
            mix(left.r, right.r, sx),
            mix(left.g, right.g, sx),
            mix(left.b, right.b, sx),
            mix(left.a, right.a, sx)
        )
    }

    fun getColor(p: Vector3): ColorRGBa {
        if (colors.size != 4) {
            return ColorRGBa.BLACK
        }
        val a = mix(colors[0], colors[1], p.x)
        val b = mix(a, colors[2], p.y)
        return mix(b, colors[3], p.z).toRGBa()
    }

    fun getColorViaRGB(p: Vector3): ColorRGBa {
        if (colors.size != 4) {
            return ColorRGBa.BLACK
        }
        val a = mix(colors[0].toRGBa(), colors[1].toRGBa(), p.x)
        val b = mix(a, colors[2].toRGBa(), p.y)
        return mix(b, colors[3].toRGBa(), p.z)
    }
}
