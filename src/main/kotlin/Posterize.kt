
import org.openrndr.application
import org.openrndr.draw.loadImage
import org.openrndr.draw.shadeStyle
import org.openrndr.extras.color.statistics.calculateHistogramRGB
    
/**
 * id: 6c89674d-a41f-4b3d-afe3-73030f12ddd7
 * description: Uses `calculateHistogramRGB` to reduce the number of colors in an image
 * tags: #histogram #posterize
 */    

/**
 * Reduces the number of colors of an image (posterization style).
 * The tricky aspect of specifying exactly how many colors you want with
 * this approach is that `binCount` defines bins per channel, so specifying 3
 * leads to 3*3*3 bins in total which is 27. Then, if you only grab 8 of
 * those, it's not a good representation of the original image. This becomes
 * obvious with higher bin counts. For example with 10*10*10 bins you get a
 * 1000 color palette. If you then grab just 8 from those they will be very
 * similar to each other, showing the most frequent colors instead of a good
 * color palette.
 * But using the smallest `binCount` for the given `numColors` also doesn't
 * seem ideal. For 8 colors the ideal seems 5 bins, not 2, 3 or 4 bins.
 * At least with the used cheeta.jpg image.
 */

fun main() = application {
    program {
        val numColors = 8
        val image = loadImage("data/images/cheeta.jpg")
        val colors = calculateHistogramRGB(image, binCount = 5)
        val topColors = colors.sortedColors().subList(0, numColors).map {
            it.first.toVector4()
        }.toTypedArray()

        extend {
            drawer.shadeStyle = shadeStyle {
                fragmentTransform = """
                        vec2 texCoord = c_boundsPosition.xy;
                        texCoord = vec2(texCoord.x, 1.0-texCoord.y);
                        vec4 origColor = texture(p_image, texCoord);
                        vec4 closestColor = vec4(0.0);
                        for(int i=0; i<$numColors; i++) {
                            if(distance(p_colors[i], origColor) < 
                              distance(closestColor, origColor)) {
                                closestColor = p_colors[i];
                            }
                        }
                        x_fill = closestColor;
                    """
                parameter("image", image)
                parameter("colors", topColors)
            }

            drawer.rectangle(drawer.bounds)
        }
    }
}
