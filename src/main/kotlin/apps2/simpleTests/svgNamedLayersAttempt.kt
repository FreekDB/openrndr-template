package apps2.simpleTests

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.drawComposition
import org.openrndr.math.Vector2
import org.openrndr.shape.Composition
import org.openrndr.shape.GroupNode
import org.openrndr.svg.toSVG
import java.io.File
    
/**
 * id: af830f5c-3a66-4e4c-b30c-582250b52866
 * description: New sketch
 * tags: #new
 */    

/**
 * ## Helper methods to create SVG files with layer names for Inkscape + AxiDraw
 *
 * Layer names in Inkscape can contain special characters like "+! %" which
 * have meaning when plotting on an AxiDraw device. Those characters can:
 * - pause plotting
 * - delay plotting (in milliseconds)
 * - specify the pen height (0-100%)
 * - specify the pen motion speed (1-100%)
 * - ignore a layer
 *
 * See [AxiDraw Layer Control](https://wiki.evilmadscientist.com/AxiDraw_Layer_Control)
 * for the correct syntax.
 *
 * Here I provide two helper methods to embed layer names in SVG files:
 *
 * 1. `setInkscapeLayer()` can be called on [GroupNode]s to set the desired Inkscape layer name.
 *
 * 2. `saveToInkscapeFile()` saves the SVG file removing the root `<g>` element. The reason for this
 * is that the AxiDraw Inkscape driver does not make use of the special characters in sublayer names,
 * therefore I upgrade sublayers to root layers.
 *
 */
fun main() = application {
    program {
        val comp = drawComposition {
            translate(drawer.bounds.center)
            stroke = ColorRGBa.BLACK

            repeat(40) {
                group {
                    circle(Vector2.ZERO, 30.0 + it * 5)
                }.setInkscapeLayer("$it+H${20+it}")

            }
        }
        comp.saveToInkscapeFile(File("/tmp/result_with_layers.svg"))
    }
}

private fun GroupNode.setInkscapeLayer(name: String) {
    attributes["inkscape:groupmode"] = "layer"
    attributes["inkscape:label"] = name
}

private fun Composition.saveToInkscapeFile(file: File) = file.writeText(
    toSVG().replace(
        Regex("""(<g\s?>(.*)</g>)""", RegexOption.DOT_MATCHES_ALL), "$2"
    )
)

