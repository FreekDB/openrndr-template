package aBeLibs.extensions

import org.openrndr.Extension
import org.openrndr.Program
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.draw.FontImageMap
import org.openrndr.draw.isolated
import org.openrndr.draw.loadFont
import org.openrndr.math.Matrix44

class FPSDisplay(private var font: FontImageMap? = null, val color: ColorRGBa) :
    Extension {
    override var enabled: Boolean = true

    private var frames = 0
    private var lastSecond = 0
    private var fps = 60

    override fun setup(program: Program) {
        if (font == null) {
            font = loadFont("data/fonts/default.otf", 12.0)
        }
        lastSecond = program.seconds.toInt()
    }

    override fun afterDraw(drawer: Drawer, program: Program) {
        val now = program.seconds.toInt()
        if (lastSecond != now) {
            lastSecond = now
            fps = frames
            frames = 0
        }
        frames++

        drawer.isolated {
            view = Matrix44.IDENTITY
            fill = color
            fontMap = font
            shadeStyle = null
            ortho()
            text(fps.toString(), width - 100.0,  40.0)
        }
    }
}
