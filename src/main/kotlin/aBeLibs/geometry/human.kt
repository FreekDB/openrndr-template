package aBeLibs.geometry

import org.openrndr.draw.VertexBuffer
import org.openrndr.draw.vertexBuffer
import org.openrndr.draw.vertexFormat
import org.openrndr.extra.noise.Random
import org.openrndr.math.Polar
import org.openrndr.math.Vector3
import org.openrndr.math.mix
import org.openrndr.shape.*

/**
 * Returns
 */
class Human(val width: Int, val height: Int) {
    private lateinit var core: ShapeContour
    private lateinit var legs: ShapeContour
    private lateinit var arms: ShapeContour
    private lateinit var head: ShapeContour
    private lateinit var geometry: VertexBuffer

    init {
        randomize()
    }
    private val Number.unit: Double
        get() = this.toDouble() * height / 96.0

    fun randomize() {
        val cox = Vector3(width * Random.double(0.4, 0.6), height * 0.6, 4.unit)
        val kneeL = cox.away(20.unit, 3.unit)
        val kneeR = cox.away(20.unit, 3.unit)
        val heelL = kneeL.away(20.unit, 1.unit)
        val heelR = kneeR.away(20.unit, 1.unit)
        val toesL = heelL.away(4.unit, 3.unit)
        val toesR = heelR.away(4.unit, 3.unit)

        val headPos = Vector3(width * Random.double(0.4, 0.6), height * 0.3, 4.unit)
        val neck = mix(cox, headPos, 0.7).away(Random.double0(height * 0.1), 2.unit)
        val elbowL = neck.away(14.unit, 3.unit)
        val elbowR = neck.away(14.unit, 3.unit)
        val wristL = elbowL.away(14.unit, 3.unit)
        val wristR = elbowR.away(14.unit, 2.unit)
        val fingerL = wristL.away(2.4.unit, 1.unit)
        val fingerR = wristR.away(2.4.unit, 1.unit)

        core = variableWidthContour(
            listOf(
                cox.copy(z = 4.unit),
                mix(cox, neck, 0.4).away(Random.double0(height * 0.1), 20.0),
                neck,
                headPos.copy(z = 4.unit)
            )
        )
        legs = variableWidthContour(listOf(toesL, heelL, kneeL, cox, kneeR, heelR, toesR))
        arms = variableWidthContour(listOf(fingerL, wristL, elbowL, neck, elbowR, wristR, fingerR))
        head = Circle(headPos.xy, 8.unit).contour

        // Maybe I could create a Shape directly skipping the intermediate contours...
        val tris = triangulate(Shape(contours()), fillRule = FillRule.NONZERO_WINDING)
        geometry = vertexBuffer(vertexFormat { position(3) }, tris.size)
        geometry.put { tris.forEach { write(it.xy0) } }
    }

    fun contours(): List<ShapeContour> {
        return listOf(core, legs, arms, head)
    }

    fun buffer() = geometry

    private fun Vector3.away(distance: Double, z: Double): Vector3 {
        return (this.xy + Polar(
            Random.double0(360.0),
            Random.double(distance * 0.5, distance)
        ).cartesian).xy0.copy(z = z)
    }

}