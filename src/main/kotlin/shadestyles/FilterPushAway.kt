package shadestyles

import org.openrndr.draw.Filter
import org.openrndr.draw.filterShaderFromCode
import org.openrndr.extra.parameters.Description
import org.openrndr.extra.parameters.DoubleParameter

/**
 *
 */

@Description("Push Away")
class PushAway : Filter(
    filterShaderFromCode(
        """#version 330
            
            uniform float amount;
            
            in vec2 v_texCoord0;
            uniform sampler2D tex0;
            uniform sampler2D tex1;
            
            out vec4 o_color;
            void main() {
                vec3 a = texture(tex0, v_texCoord0).rgb;
                vec3 b = texture(tex1, v_texCoord0).rgb;
                
                vec2 uv = v_texCoord0;
                
                o_color = vec4(a + b, 1.0);
            }
        """.trimIndent(), "Push Away"
    )
) {

    @DoubleParameter("amount", 0.0, 1.0)
    var amount: Double by parameters

    init {
        amount = 1.0
    }
}
