package apps5

import com.soywiz.korma.random.randomWithWeights
import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.boofcv.binding.resizeTo
import org.openrndr.color.ColorRGBa
import org.openrndr.color.rgb
import org.openrndr.draw.*
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.fx.blend.Add
import org.openrndr.extra.fx.blend.Subtract
import org.openrndr.extra.fx.blur.ApproximateGaussianBlur
import org.openrndr.extra.fx.color.ColorCorrection
import org.openrndr.extra.fx.color.Invert
import org.openrndr.extra.noise.Random
import org.openrndr.extra.noise.random
import org.openrndr.extra.palette.PaletteStudio
import org.openrndr.extra.shapes.grid
import org.openrndr.extras.meshgenerators.toMesh
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import kotlin.system.exitProcess

/**
 * Simple program creating a grid of circles shaded to look like 3D,
 * even if they are 2D.
 */

fun main() = application {
    configure {
        width = 1200
        height = 1200
    }

    program {
        // Textures
        val drawLayer = renderTarget(
            width, height,
            multisample = BufferMultisample.SampleCount(4)
        ) { colorBuffer() }
        val drawLayerBW = renderTarget(
            width, height,
            multisample = BufferMultisample.SampleCount(4)
        ) { colorBuffer() }
        val final = colorBuffer(width, height)
        var display: ColorBuffer? = null

        val drawLayerRes = final.createEquivalent()
        val blurredLayer = final.createEquivalent()
        val added = final.createEquivalent()
        val colorCorrLayer = final.createEquivalent()
        val whiteOnBlack = final.createEquivalent()
        val blackOnWhite = final.createEquivalent()
        val whiteOnBlackBlurred = final.createEquivalent()
        val dirt = final.createEquivalent()
        val dirt2 = final.createEquivalent()
        val texLowContrast = final.createEquivalent()
        val tex = loadImage("/home/funpro/Pictures/2021/06/DSC00101.JPG")
            .resizeTo(width, height)

        // Effects
        val add = Add()
        val sub = Subtract()
        val invert = Invert()
        val colorCorrection = ColorCorrection().apply {
            brightness = -0.5
            contrast = 1.0
            saturation = -0.3
            gamma = 1.5
            opacity = 1.0
        }
        val dirtBlur = ApproximateGaussianBlur().apply {
            window = 20
            spread = 10.0
            gain = 1.01
            sigma = 2.0
        }
        val glowBlur = ApproximateGaussianBlur().apply {
            window = 25
            spread = 2.0
            gain = 1.0
            sigma = 10.0
        }
        val lowContrast = ColorCorrection().apply {
            brightness = -0.4
            contrast = -0.7
        }
        val lowContrast2 = ColorCorrection().apply {
            brightness = -0.15
            contrast = -0.2
        }

        val palettes = PaletteStudio(
            sortBy = PaletteStudio.SortBy.DARKEST,
            collection = PaletteStudio.Collections.TWO
        )
        val colorWeights = List(palettes.colors.size) {
            1.0 / (1.0 + it * it)
        }
        val gradientDeform = shadeStyle {
            fragmentTransform = """
                    vec2 coord = (va_texCoord0.xy - 0.5 + p_offset);

                    float angle = radians(p_rotation);
                    float cr = cos(angle);
                    float sr = sin(angle);
                    mat2 rm = mat2(cr, -sr, sr, cr);
                    vec2 rc = rm * coord;
                    float f = clamp(rc.y + 0.5, 0.0, 1.0);            

                    vec4 gradient = mix(p_color0, p_color1, pow(f, p_exponent));
                    x_fill *= gradient;
                    x_fill.rg += sin(gl_FragCoord.y * 0.005 + vec2(0., 1.)) *
                     0.1 - 0.05;
                    x_fill.b += sin(gl_FragCoord.x * 0.005) * 0.1 - 0.05;
                """

            vertexPreamble = """
                    float slopeFromT (float t, float A, float B, float C){
                      return 1.0/(3.0*A*t*t + 2.0*B*t + C); 
                    }
                    float xFromT (float t, float A, float B, float C, float D){
                      return A*(t*t*t) + B*(t*t) + C*t + D;
                    }
                    float yFromT (float t, float E, float F, float G, float H){
                      return E*(t*t*t) + F*(t*t) + G*t + H;
                    }
                    float cubicBezier (float a, float b, float c, float d, float x){
                      float y0a = 0.00; // initial y
                      float x0a = 0.00; // initial x 
                      float y1a = b;    // 1st influence y   
                      float x1a = a;    // 1st influence x 
                      float y2a = d;    // 2nd influence y
                      float x2a = c;    // 2nd influence x
                      float y3a = 1.00; // final y 
                      float x3a = 1.00; // final x 
                    
                      float A =   x3a - 3*x2a + 3*x1a - x0a;
                      float B = 3*x2a - 6*x1a + 3*x0a;
                      float C = 3*x1a - 3*x0a;   
                      float D =   x0a;
                    
                      float E =   y3a - 3*y2a + 3*y1a - y0a;    
                      float F = 3*y2a - 6*y1a + 3*y0a;             
                      float G = 3*y1a - 3*y0a;             
                      float H =   y0a;
                    
                      // Solve for t given x (using Newton-Raphelson), then solve for y given t.
                      // Assume for the first guess that t = x.
                      float currentt = x;
                      int nRefinementIterations = 5;
                      for (int i=0; i < nRefinementIterations; i++){
                        float currentx = xFromT (currentt, A,B,C,D); 
                        float currentslope = slopeFromT (currentt, A,B,C);
                        currentt -= (currentx - x)*(currentslope);
                        currentt = clamp(currentt, 0.0, 1.0);
                      } 
                    
                      return yFromT(currentt, E, F, G, H);
                    }                
            """.trimIndent()

            vertexTransform = """
                    vec2 center = u_viewDimensions * 0.513;
                    vec2 diff = x_position.xy - center;
                    float l = 1.0 - min(length(diff) / 500.0, 1.0);
                    float amt = cubicBezier(0.10, 0.0, 0.15, 1.0, l);
                    x_position.xy += normalize(diff) * amt * 100.0; 
                """.trimIndent()
        }
        gradientDeform.parameter("exponent", 4.0)
        gradientDeform.parameter("rotation", 90.0)
        gradientDeform.parameter("offset", Vector2.ZERO)

        fun grid(rect: Rectangle, probability: Double, bw: Boolean = false) {
            if (rect.width > 20 && rect.height > 20 && Random.bool(probability)) {
                rect.grid(
                    Random.int(1, 4),
                    Random.int(1, 4),
                    0.0, 0.0, 5.0, 5.0
                ).flatten().forEach {
                    grid(it, probability * 0.9, bw)
                }
            } else {
                val colors = if (bw) {
                    drawer.stroke = rgb(0.2, 0.3, 0.4)
                    Pair(rgb(0.1, 0.2, 0.3), rgb(0.2, 0.3, 0.4))
                } else {
                    drawer.stroke = null
                    val color = palettes.colors.randomWithWeights(colorWeights)
                    Pair(
                        color.shade(random(0.8, 1.05)),
                        color.shade(random(0.7, 0.9))
                    )
                }
                gradientDeform.parameter("color0", colors.first)
                gradientDeform.parameter("color1", colors.second)

                val mesh = rect.toMesh(1.0)
                drawer.vertexBuffer(mesh, DrawPrimitive.TRIANGLES)
                mesh.destroy()
            }
        }

        fun makeIt() {
            val seed = seconds.toString()
            palettes.randomPalette()
            drawer.shadeStyle = gradientDeform

            // ---
            Random.seed = seed
            drawer.isolatedWithTarget(drawLayer) {
                clear(palettes.background)
                grid(drawer.bounds.offsetEdges(-10.0), 1.0)
            }
            drawLayer.colorBuffer(0).copyTo(drawLayerRes)
            colorCorrection.apply(drawLayerRes, colorCorrLayer)
            glowBlur.apply(colorCorrLayer, blurredLayer)
            add.apply(arrayOf(drawLayerRes, blurredLayer), added)

            // ---
            Random.seed = seed
            drawer.isolatedWithTarget(drawLayerBW) {
                clear(ColorRGBa.BLACK)
                grid(drawer.bounds.offsetEdges(-10.0), 1.0, true)
            }
            drawLayerBW.colorBuffer(0).copyTo(whiteOnBlack)
            invert.apply(whiteOnBlack, blackOnWhite)
            dirtBlur.apply(blackOnWhite, whiteOnBlackBlurred)
            sub.apply(arrayOf(whiteOnBlackBlurred, blackOnWhite), dirt)
            lowContrast.apply(tex, texLowContrast)
            sub.apply(arrayOf(dirt, texLowContrast), dirt2)
            lowContrast2.apply(dirt2, texLowContrast)
            sub.apply(arrayOf(added, texLowContrast), final)
            display = final

            //System.gc()
        }

        makeIt()

        extend(Screenshots())
        extend {
            display?.run { drawer.image(this) }
        }
        keyboard.keyUp.listen {
            if (it.key == KEY_ESCAPE) {
                exitProcess(0)
            }
            if (it.name == "n") {
                makeIt()
            }
        }
    }
}