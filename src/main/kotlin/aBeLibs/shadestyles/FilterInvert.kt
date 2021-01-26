package aBeLibs.shadestyles

import org.openrndr.draw.Filter
import org.openrndr.draw.filterShaderFromCode
import org.openrndr.extra.parameters.Description
import org.openrndr.extra.parameters.DoubleParameter

/**
 * Invert filter, for testing purposes
 */
@Description("Approximate Gaussian blur")
class FilterInvert : Filter(
    filterShaderFromCode(
        """
            #version 330
            
            uniform float a;
            in vec2 v_texCoord0;
            uniform sampler2D tex0;
            
            out vec4 o_color;
            void main() {
                vec4 c = texture(tex0, v_texCoord0);
                o_color = vec4(a - c.rgb, c.a);
            }
        """.trimIndent(), "Invert"
    )
) {

    @DoubleParameter("a", 0.0, 1.0)
    var a: Double by parameters

    init {
        a = 1.0
    }
}