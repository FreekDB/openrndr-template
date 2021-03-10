package latest

import org.openrndr.application
import org.openrndr.draw.*

fun main() {
    application {
        program {
            val triangles = 60
            val vb = vertexBuffer(
                vertexFormat {
                    // Must be multiple of 8! 3+3+2 = 8
                    position(3)
                    attribute("pad1", VertexElementType.FLOAT32)
                    color(3)
                    attribute("pad2", VertexElementType.FLOAT32)
                },triangles * 3
            )

            val computeShaderCode = """
            #version 430
            layout(local_size_x = 1, local_size_y = 1) in;
            
            uniform float time;
            
            struct VBOElement {
                vec3 position;
                float pad1;
                vec3 color;
                float pad2;
            };
            
            buffer outputBuffer {
                VBOElement vertices[];
            };
            
            void main() {
                for (int i=0; i<$triangles; ++i) {
                    int ii = i*3;
                    float r = 100.0 + i;
                    float x = ${width/2.0} + r * sin(time + i);
                    float y = ${height/2.0} + r * cos(time + i); 
                    
                    vertices[ii  ].position = vec3(x, y-10, 0.0);
                    vertices[ii+1].position = vec3(x, y+10, 0.0);
                    vertices[ii+2].position = vec3(x+10, y, 0.0);
                    
                    vertices[ii  ].color = vec3(1.0, 0.0, 0.0);
                    vertices[ii+1].color = vec3(0.0, 1.0, 0.0);
                    vertices[ii+2].color = vec3(0.0, 0.0, 1.0);
                }
            }
            """.trimIndent()

            val computeShader = ComputeShader.fromCode(computeShaderCode)

            extend {
                computeShader.buffer("outputBuffer", vb)
                computeShader.uniform("time", seconds)
                computeShader.execute()

                drawer.shadeStyle = shadeStyle {
                    fragmentTransform = "x_fill.rgb = va_color;"
                }

                drawer.vertexBuffer(vb, DrawPrimitive.TRIANGLES)
            }
        }
    }
}