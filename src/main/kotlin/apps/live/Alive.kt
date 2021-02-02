package apps.live

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.extra.fx.blur.ApproximateGaussianBlur
import org.openrndr.extra.noise.Random
import org.openrndr.extra.noise.uniform
import org.openrndr.math.Vector2

fun main() {
    application {
        configure {
            width = 1500
            height = 900
        }
        program {
            var cb = colorBuffer(width, height)

            extend {
                if (frameCount % 60 == 0) {
                    cb = treeShadowTexture(drawer, width, height)
                }
                drawer.image(cb)
            }
        }
    }
}

fun treeShadowTexture(drawer: Drawer, width: Int, height: Int): ColorBuffer {
    val rt = renderTarget(width, height) {
        colorBuffer()
    }
    val result = renderTarget(width, height) {
        colorBuffer()
    }
    val blurred = colorBuffer(width, height)
    val blur = ApproximateGaussianBlur()
    blur.window = 10
    blur.spread = 4.0
    blur.sigma = 15.0

    Random.seed = System.currentTimeMillis().toString()
    drawer.isolatedWithTarget(rt) {
        drawer.clear(ColorRGBa.BLACK) // ColorHSVa(0.4, 0.1, 0.7).toRGBa()
        drawer.stroke = null
        for (i in 0..300) {
            val pos = Vector2.uniform(Vector2.ZERO, bounds.dimensions)
            val r = Random.simplex(pos * 0.001)
            if (r > -0.1) {
                drawer.fill = ColorRGBa.WHITE.opacify(Random.double(0.5, 0.7))
                for (j in 0..1) {
                    val offset = Vector2.uniform(
                        Vector2.ONE * -15.0,
                        Vector2.ONE * 15.0
                    )
                    drawer.circle(pos + offset, r * 25.0 + 30.0)
                }
            }
        }
    }
    blur.apply(rt.colorBuffer(0), blurred)
    drawer.isolatedWithTarget(result) {
        drawer.shadeStyle = shadeStyle {
            fragmentPreamble = """
                        float doubleCircleSigmoid (float x, float a){
                          float min_param_a = 0.0;
                          float max_param_a = 1.0;
                          a = max(min_param_a, min(max_param_a, a)); 
                        
                          float y = 0.0;
                          if (x<=a){
                            y = a - sqrt(a*a - x*x);
                          } else {
                            y = a + sqrt(pow(1.0-a, 2.0) - pow(x-1.0, 2.0));
                          }
                          return y;
                        }
                        
                        float quadraticBezier (float x, float a, float b){
                          // adapted from BEZMATH.PS (1993)
                          // by Don Lancaster, SYNERGETICS Inc. 
                          // http://www.tinaja.com/text/bezmath.html
                        
                          float epsilon = 0.00001;
                          a = max(0, min(1, a)); 
                          b = max(0, min(1, b)); 
                          if (a == 0.5){
                            a += epsilon;
                          }
                          
                          // solve t from x (an inverse operation)
                          float om2a = 1 - 2*a;
                          float t = (sqrt(a*a + om2a*x) - a)/om2a;
                          float y = (1-2*b)*(t*t) + (2*b)*t;
                          return y;
                        }
                    """.trimIndent()
            fragmentTransform = """
//                        x_fill.rgb = pow(x_fill.rgb, vec3(2.0));

                        x_fill.r = doubleCircleSigmoid(x_fill.r, 0.9);
                        x_fill.g = doubleCircleSigmoid(x_fill.g, 0.9);
                        x_fill.b = doubleCircleSigmoid(x_fill.b, 0.9);

//                        x_fill.r = quadraticBezier(x_fill.r, 0.9, 0.1);
//                        x_fill.g = quadraticBezier(x_fill.g, 0.9, 0.1);
//                        x_fill.b = quadraticBezier(x_fill.b, 0.9, 0.1);
                    """.trimIndent()
        }
        drawer.image(blurred)
    }
    return result.colorBuffer(0)
}
