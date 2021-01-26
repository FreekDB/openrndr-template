package aBeLibs.fx

import org.openrndr.draw.*
import org.openrndr.extra.parameters.BooleanParameter
import org.openrndr.extra.parameters.Description
import org.openrndr.extra.parameters.DoubleParameter

@Description("Color correction")
class WideColorCorrection : Filter(filterShaderFromUrl("file:///home/funpro/src/OR/orx/orx-fx/src/main/resources/org/openrndr/extra/fx/gl3/color/color-correction.frag")) {
    @DoubleParameter("brightness", -5.0, 5.0, order = 0)
    var brightness: Double by parameters

    @DoubleParameter("contrast", -5.0, 5.0, order = 1)
    var contrast: Double by parameters

    @DoubleParameter("saturation", -5.0, 5.0, order = 2)
    var saturation: Double by parameters

    @DoubleParameter("hue shift", -180.0, 180.0, order = 3)
    var hueShift: Double by parameters

    @DoubleParameter("gamma", 0.0, 5.0, order = 4)
    var gamma: Double by parameters

    @DoubleParameter("opacity", 0.0, 1.0, order = 5)
    var opacity: Double by parameters

    @BooleanParameter("clamp", order = 6)
    var clamped: Boolean by parameters

    init {
        contrast = 0.0
        brightness = 0.0
        saturation = 0.0
        hueShift = 0.0
        gamma = 1.0
        opacity = 1.0
        clamped = true
    }
}