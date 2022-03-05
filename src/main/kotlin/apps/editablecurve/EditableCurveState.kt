@file:Suppress("MemberVisibilityCanBePrivate")

package apps.editablecurve

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.openrndr.color.ColorRGBa
import org.openrndr.math.Vector2
import org.openrndr.math.clamp
import org.openrndr.panel.elements.Slider
import org.openrndr.shape.ShapeContour
import java.io.File

/**
 * id: 37010faa-a0c7-4907-81f6-20ea846d3256
 * description: New sketch
 * tags: #new
 */

object EditableCurveState {
    lateinit var sNumSubcurves: Slider
    lateinit var sSeparation: Slider
    var winSize = Vector2(600.0)
    var bgColor = ColorRGBa.WHITE.shade(0.95)
    var activeCurve: EditableCurve? = null
    var curves = mutableListOf<EditableCurve>()
    var segments = mutableListOf<ShapeContour>()
    var saveSVG = false
    private var curvesNeedUpdate = false
    private var mouseClickStart = Vector2(0.0)

    fun refreshCurves() {
        segments.clear()
        curves.forEach { it.addSegmentsTo(segments) }
    }

    private fun selectCurve(pos: Vector2): EditableCurve? {
        return curves.minByOrNull { it.distanceTo(pos) }
    }

    fun addCurve(): EditableCurve {
        val c = EditableCurve()
        curves.add(c)

        activeCurve = curves.lastOrNull()

        c.randomizePoints(winSize)
        c.addSegmentsTo(segments)
        return c
    }

    fun removeCurve() {
        curves.remove(activeCurve)
        activeCurve = curves.lastOrNull()
    }

    fun removeAllCurves() {
        segments.clear()
        curves.clear()
        activeCurve = null
    }

    fun loadFile(file: File) {
        val gson = Gson()
        val typeToken = object : TypeToken<MutableList<EditableCurve>>() {}
        curves = gson.fromJson(file.readText(), typeToken.type)
        curves.forEach { it.update() }
        refreshCurves()
    }

    fun saveFile(file: File) {
        val gson = Gson()
        val json = gson.toJson(curves)
        file.writeText(json)
    }

    fun setSubcurves(num: Double) {
        activeCurve?.numSubcurves = num.toInt()
        refreshCurves()
    }

    fun setSeparation(sep: Double) {
        activeCurve?.separation = sep
        refreshCurves()
    }

    fun onMouseUp(pos: Vector2) {
        if (curves.size == 0) {
            return
        }
        if (curvesNeedUpdate) {
            refreshCurves()
            curvesNeedUpdate = false
        }
        val dist = (pos - mouseClickStart).length
        if (dist < 10) {
            activeCurve = selectCurve(pos)
            activeCurve?.let {
                sNumSubcurves.value = it.numSubcurves.toDouble()
                sSeparation.value = it.separation
            }
        }
    }

    fun onMouseDown(pos: Vector2) {
        activeCurve?.mousePressed(pos)
        mouseClickStart = pos
    }

    fun onMouseDrag(pos: Vector2) {
        activeCurve?.let { curve ->
            curve.mouseDragged(
                Vector2(
                    clamp(pos.x, winSize.x * 0.1, winSize.x * 0.9),
                    clamp(pos.y, winSize.y * 0.1, winSize.y * 0.9)
                )
            )
            // TODO: can be optimized by observing if a curve was modified
            curvesNeedUpdate = true
        }
    }
}
