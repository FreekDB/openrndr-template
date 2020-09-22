package apps2.city

import org.openrndr.draw.Drawer
import org.openrndr.draw.LineCap
import org.openrndr.draw.isolated
import org.openrndr.extra.noise.Random
import org.openrndr.math.Vector2
import org.openrndr.shape.LineSegment

val streetInventory = listOf(
    LineSegment(-hf, 0.0, 0.0, 0.0), //ch
    LineSegment(0.0, 0.0, hf, 0.0), //ch
    LineSegment(-hf, hf, 0.0, hf), //bh
    LineSegment(0.0, hf, hf, hf), //bh

    LineSegment(0.0, -hf, 0.0, 0.0), //cv
    LineSegment(0.0, 0.0, 0.0, hf), //cv
    LineSegment(hf, -hf, hf, 0.0), //rv
    LineSegment(hf, 0.0, hf, hf), //rv

    LineSegment(-hf, -hf, 0.0, 0.0), //x
    LineSegment(0.0, 0.0, hf, hf), //x
    LineSegment(-hf, hf, 0.0, 0.0), //x
    LineSegment(0.0, 0.0, hf, -hf)   //x
)

class Tile(private val pos: Vector2) {
    private val pStreets = mutableListOf<LineSegment>()
    private val pStreetWidths = mutableListOf<Double>()
    private val pHouses = mutableListOf<LineSegment>()
    private val pHouseWidths = mutableListOf<Double>()

    val streets = pStreets as List<LineSegment>
    val streetWidths = pStreetWidths as List<Double>
    val houses = pHouses as List<LineSegment>
    val houseWidths = pHouseWidths as List<Double>

    val contacts = 0

    fun build() {
        pStreets.clear()
        pStreetWidths.clear()
        pHouses.clear()
        pHouseWidths.clear()

        val count = Random.int(3, 9)
        val offset = pos * sz
        streetInventory.shuffled().subList(0, count).forEach { street ->
            pStreets.add(
                LineSegment(
                    street.start + offset,
                    street.end + offset
                )
            )
            pStreetWidths.add(sz / 40)

            val center = street.position(0.5)
            val normal = street.normal * if (Random.bool()) 1.0 else -1.0
            val dist0 = sz * Random.double(0.03, 0.05)
            val dist1 = sz * Random.double(0.10, 0.25)
            val house = LineSegment(
                center + normal * dist0 + offset,
                center + normal * dist1 + offset
            )
            pHouses.add(house)
            pHouseWidths.add(sz * Random.double(0.1, 0.2))
        }
    }

    fun draw(d: Drawer) {
        d.isolated {
            lineCap = LineCap.ROUND
            pStreets.forEachIndexed { i, street ->
                strokeWeight = pStreetWidths[i]
                lineSegment(street)
            }

            lineCap = LineCap.BUTT
            pHouses.forEachIndexed { i, house ->
                strokeWeight = pHouseWidths[i]
                lineSegment(house)
            }
        }
    }
}