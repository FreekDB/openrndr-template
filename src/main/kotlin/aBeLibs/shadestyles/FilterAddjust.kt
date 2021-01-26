package aBeLibs.shadestyles

import org.openrndr.draw.ColorBuffer
import org.openrndr.draw.Filter
import org.openrndr.draw.filterShaderFromCode
import org.openrndr.extra.parameters.Description
import org.openrndr.extra.parameters.DoubleParameter

/**
 * This filter is basically the Add blend mode, with some
 * differences:
 * - amount sets the brightness of the second texture. If
 *   set to 0.0 you get only the first texture.
 * - lumadist controls how much brightness distorts the
 *   second texture. This simulates depth.
 */

@Description("Add & Adjust")
class Addjust : Filter(
    filterShaderFromCode(
        """#version 330
            
            uniform float amount;
            uniform float lumadist;
            
            in vec2 v_texCoord0;
            uniform sampler2D tex0;
            uniform sampler2D tex1;
            
            out vec4 o_color;
            void main() {
                vec3 a = texture(tex0, v_texCoord0).rgb;
                
                float luma = dot(a, vec3(0.299, 0.587, 0.114));
                vec2 uv = v_texCoord0 - 0.5;
                uv /= 1.0 + luma * lumadist;
                uv += 0.5;
                
                vec3 b = texture(tex1, uv).rgb;
                o_color = vec4(a + b * amount, 1.0);
            }
        """.trimIndent(), "Addjust"
    )
) {

    @DoubleParameter("amount", 0.0, 1.0)
    var amount: Double by parameters

    @DoubleParameter("luma distortion", 0.0, 1.0)
    var lumadist: Double by parameters

    init {
        amount = 1.0
        lumadist = 0.1
    }
}
