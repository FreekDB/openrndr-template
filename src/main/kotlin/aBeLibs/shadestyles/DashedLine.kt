package aBeLibs.shadestyles

import org.openrndr.draw.ShadeStyle
import org.openrndr.extra.parameters.Description
import org.openrndr.extra.parameters.DoubleParameter

/**
 * A shadeStyle for dashed lines with two parameters:
 * - length: the length of each dash
 * - margin: the separation between dashes
 */
@Description("Dashed line")
class DashedLine(
    length: Double = 20.0,
    margin: Double = 5.0
) : ShadeStyle() {

    @DoubleParameter("length", 0.0, 50.0)
    var length: Double by Parameter()

    @DoubleParameter("margin", 0.0, 50.0)
    var margin: Double by Parameter()

    init {
        this.length = length
        this.margin = margin

        fragmentTransform = """
            float l = mod(c_contourPosition, p_length + p_margin);
            x_stroke.a *= 1.0 - step(p_length, l);
        """
    }
}
