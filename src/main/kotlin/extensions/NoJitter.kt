package extensions

import org.openrndr.Extension
import org.openrndr.Program
import org.openrndr.draw.*
import org.openrndr.extra.fx.color.SetBackground

class NoJitter : Extension {
    override var enabled: Boolean = true

    private val rt = renderTarget(16, 16) {
        colorBuffer()
    }
    private val filtered = colorBuffer(16, 16)
    private val simpleFilter = SetBackground()

    override fun beforeDraw(drawer: Drawer, program: Program) {
        simpleFilter.apply(rt.colorBuffer(0), filtered)
    }
}