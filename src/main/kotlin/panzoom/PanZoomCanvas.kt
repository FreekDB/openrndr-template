package panzoom

import org.openrndr.MouseEvent
import org.openrndr.draw.Drawer
import org.openrndr.draw.renderTarget
import org.openrndr.math.Vector2
import org.openrndr.math.clamp
import org.openrndr.shape.Rectangle
import kotlin.math.max
import kotlin.math.min

class PanZoomCanvas(w: Int, h: Int) {
    private var zoom = 1.0
    private var zoomCurr = 1.0
    private var offset = Vector2(0.0)
    private var offsetCurr = Vector2(0.0)

    private lateinit var viewport: Rectangle

    val rt = renderTarget(w, h) {
        colorBuffer()
    }

    fun setViewport(viewport: Rectangle) {
        this.viewport = viewport
    }

    private fun constrainView() {
        offset = Vector2(
            clamp(offset.x, 0.0, rt.width * (1.0 - zoom)),
            clamp(offset.y, 0.0, rt.height * (1.0 - zoom))
        )
    }

    fun draw(drawer: Drawer) {
        zoomCurr = zoomCurr * 0.8 + zoom * 0.2 // lerp() or mix()
        offsetCurr = offsetCurr * 0.8 + offset * 0.2
        val sz = max(rt.width, rt.height)
        val source = Rectangle(offsetCurr, sz * zoomCurr, sz * zoomCurr)
        drawer.image(rt.colorBuffer(0), source, viewport)
    }

    fun inside(pos: Vector2): Boolean {
        return viewport.contains(pos)
    }

    fun globalToLocal(pos: Vector2): Vector2 {
        return offsetCurr + Vector2(
            rt.width * (pos.x - viewport.x) / viewport.width,
            rt.height * (pos.y - viewport.y) / viewport.height
        ) * zoomCurr
    }

    fun drag(ev: MouseEvent) {
        offset -= ev.dragDisplacement
        constrainView()
    }

    fun wheel(ev: MouseEvent) {
        var newZoom = zoom * (1.0 - 0.1 * ev.rotation.y)
        val k = min(
            viewport.width / rt.width,
            viewport.height / rt.height
        )
        newZoom = clamp(newZoom, k, 1.0)
        val zoomChange = newZoom - zoom
        offset -= (ev.position - viewport.corner) * zoomChange / k

        zoom = newZoom
        constrainView()
    }
}