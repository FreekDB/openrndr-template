package apps2.city

import aBeLibs.geometry.intersects
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.Random
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.math.bezier
import org.openrndr.math.map
import org.openrndr.shape.*
import java.io.File
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

fun main() = application {
    configure {
        width = 1024
        height = 1024
    }
    program {
        val contours = mutableListOf<ShapeContour>()
        val innerCitySegments = mutableListOf<LineSegment>()
        val houses = mutableListOf<LineSegment>()
        val houseWidths = mutableListOf<Double>()
        val houseContours = mutableListOf<ShapeContour>()
        var mapBounds = Rectangle(Vector2.ZERO, 1.0, 1.0)
        var saveCounter = 0

        fun generate() {
            Random.seed = System.currentTimeMillis().toString()

            listOf(contours, innerCitySegments, houses, houseWidths, houseContours).forEach { it.clear() }

            // --- City center
            val cityCenter = ShapeContour.fromPoints(
                List(30) {
                    val a = PI * 2 * it / 30.0
                    Polar(
                        Math.toDegrees(a),
                        Random.simplex(0.35 * sin(a), 0.35 * cos(a)) * 70 + 100 +
                                Random.simplex(1.35 * sin(a), 1.35 * cos(a)) * 20
                    ).cartesian
                }, true
            )
            contours.add(cityCenter)

            // --- Radial roads
            val radialsCount = Random.int(5, 11)
            val radials = List(radialsCount) {
                val pc = it / radialsCount.toDouble()
                var pos = cityCenter.position(pc)
                val distToCenter = pos - cityCenter.bounds.center
                var angle = Math.toDegrees(atan2(distToCenter.y, distToCenter.x))
                val steps = Random.int(7, 20)
                ShapeContour.fromPoints(
                    List(steps) { step ->
                        if (step == 0) {
                            pos
                        } else {
                            angle += Random.gaussian(0.0, 10.0)
                            pos += Polar(angle, Random.double(10.0, 40.0)).cartesian
                            pos
                        }
                    }, false
                )
            }
            contours.addAll(radials)

            // --- Roads connecting radial roads
            val connCount = Random.int(3, 11)
            val connections = mutableListOf<ShapeContour>()
            while (connections.size < connCount) {
                val roadA = Random.int0(radials.size)
                val shift = if (Random.bool(0.85)) 2 else 1
                val roadB = (roadA + shift) % radials.size
                val pca = Random.double(0.2, 0.8)
                val pcb = Random.double(0.2, 0.8)
                val p0 = radials[roadA].position(pca)
                val p1 = radials[roadB].position(pcb)
                val d = p0.distanceTo(p1) / 3
                val c0 = p0 - radials[roadA].normal(pca) * d
                val c1 = p1 + radials[roadB].normal(pcb) * d
                val steps = 3 + (d / 40).toInt()
                val connection = ShapeContour.fromPoints(
                    List(steps) { step -> bezier(p0, c0, c1, p1, step / (steps - 1.0)) }, false
                )
                if (connections.all { c -> connection.intersects(c) == Vector2.INFINITY } &&
                    connection.intersects(cityCenter) == Vector2.INFINITY) {
                    connections.add(connection)
                    contours.add(connection)
                }
            }

            // --- Perpendicular roads to connections and radials
            val sources = connections + radials
            val perpCount = contours.size + Random.int(20, 40)
            while (contours.size < perpCount) {
                val source = Random.pick(sources)
                val where = Random.double0()
                val start = source.position(where)
                val normal = source.normal(where) * 8.0 * Random.sign()
                val steps = Random.int(3, 10)
                val perp = ShapeContour.fromPoints(
                    List(steps) {
                        start + normal * (it - 0.1) + Random.vector2()
                    }, false
                )
                val intCount = contours.count { other -> perp.intersects(other) != Vector2.INFINITY }
                if (intCount == 1) {
                    contours.add(perp)
                }
            }

            mapBounds = (contours.map { it.bounds }).bounds.offsetEdges(10.0)

            // ---  Fill city center
            // - [x] make grid of segments Horizontal, Vertical, some diagonal covering bounds
            val area = cityCenter.bounds
            val xStep = Vector2(area.width / 10, 0.0)
            val yStep = Vector2(0.0, area.height / 10)
            for (x in 0 until 10) {
                for (y in 0 until 10) {
                    val pos = area.corner + Vector2(x * 0.1, y * 0.1) * area.dimensions

                    if (Random.bool(0.5)) {
                        innerCitySegments.add(LineSegment(pos, pos + xStep))
                    }

                    if (Random.bool(0.5)) {
                        innerCitySegments.add(LineSegment(pos, pos + yStep))
                    }

                    if (Random.bool(0.2)) {
                        innerCitySegments.add(LineSegment(pos + xStep, pos + yStep))
                    } else if (Random.bool(0.2)) {
                        innerCitySegments.add(LineSegment(pos, pos + xStep + yStep))
                    }
                }
            }
            // - [x] displace start and end points using noise
            innerCitySegments.replaceAll {
                val offsetStart = Vector2(
                    Random.simplex(it.start * 0.1) * 10.0,
                    Random.simplex(it.start.yx * 0.1) * 10.0
                )
                val offsetEnd = Vector2(
                    Random.simplex(it.end * 0.1) * 10.0,
                    Random.simplex(it.end.yx * 0.1) * 10.0
                )
                LineSegment(it.start + offsetStart, it.end + offsetEnd)
            }

            // - [x] delete segments completely outside of the center
            // - [x] delete segments with start/end too near cityCenter
            innerCitySegments.removeIf {
                val startInside = cityCenter.contains(it.start)
                val endInside = cityCenter.contains(it.end)
                // totally outside
                (!startInside && !endInside) ||
                        // totally inside but too near city center
                        (startInside && endInside && (cityCenter.on(it.start) != null ||
                                cityCenter.on(it.end) != null))
            }

            // - [x] for segments intersecting cityCenter, cut the part outside the center
            innerCitySegments.replaceAll {
                val startInside = cityCenter.contains(it.start)
                val endInside = cityCenter.contains(it.end)
                if (startInside != endInside) {
                    val int = cityCenter.intersects(Segment(it.start, it.end))
                    LineSegment(if (startInside) it.start else it.end, int)
                } else {
                    it
                }
            }

            // - [x] delete if street too short (cut in last step)
            innerCitySegments.removeIf {
                it.start.distanceTo(it.end) < 10.0
            }

            fun tryAddHouse(pos: Vector2, normal: Vector2): Boolean {
                val tan = normal.perpendicular()
                val c0 = normal * Random.double(2.0, 4.0)
                val c1 = normal * Random.double(8.0, 16.0)
                val w = Random.double(4.0, 8.0)
                val current = ShapeContour.fromPoints(
                    listOf(
                        pos + c0 + tan * w,
                        pos + c1 + tan * w,
                        pos + c1 - tan * w,
                        pos + c0 - tan * w
                    ), true
                )
                val noHouseOverlaps = houseContours.all { other ->
                    other.intersects(current) == Vector2.INFINITY &&
                            !current.contains(other.bounds.center)
                }
                val noRoadOverlaps: Boolean by lazy {
                    contours.all { road -> road.intersects(current) == Vector2.INFINITY }
                }
                val noCityCenterOverlaps: Boolean by lazy {
                    innerCitySegments.all { street -> current.intersects(street) == Vector2.INFINITY }
                }
                return if (noHouseOverlaps && noRoadOverlaps && noCityCenterOverlaps) {
                    houses.add(LineSegment(pos + c0, pos + c1))
                    houseWidths.add(w)
                    houseContours.add(current)
                    true
                } else {
                    false
                }
            }

            var tries = 0
            while (tries < 50) {
                val road = Random.pick(contours)
                val pc = Random.double0()
                val pos = road.position(pc)
                val validPos = Random.simplex(pos * 0.01) > 0.0
                val normal = road.normal(pc) * Random.sign()
                if (validPos) {
                    if (tryAddHouse(pos, normal)) {
                        tries = 0
                    } else {
                        tries++
                    }
                }
            }

            tries = 0
            while (tries < 100) {
                val road = Random.pick(innerCitySegments)
                val pc = 0.5
                val pos = road.position(pc)
                val normal = road.normal * Random.sign()
                if (tryAddHouse(pos, normal)) {
                    tries = 0
                } else {
                    tries++
                }
            }
        }

        fun save() {
            val collada = Collada()

            val streetUVs = innerCitySegments.map { road ->
                Rectangle.fromCenter(
                    road.position(0.5).map(
                        mapBounds.corner, mapBounds.corner + mapBounds.dimensions,
                        Vector2.ZERO, Vector2.ONE
                    ), 0.0, 0.0
                )
            }
            val streetWidths = innerCitySegments.map { Random.double(1.0, 3.0) }

            val roadWidths = mutableListOf<Double>()
            val roads = contours.flatMap { road ->
                val width = Random.double(2.0, 4.0)
                roadWidths.addAll(road.segments.map { width })
                road.segments.map { LineSegment(it.start, it.end) }
            }
            val roadUVs = roads.map { road ->
                Rectangle.fromCenter(
                    road.position(0.5).map(
                        mapBounds.corner, mapBounds.corner + mapBounds.dimensions,
                        Vector2.ZERO, Vector2.ONE
                    ), 0.0, 0.0
                )
            }
            collada.addLineSegments(
                "streets",
                innerCitySegments + roads,
                streetWidths + roadWidths,
                streetUVs + roadUVs
            )
//            collada.addLineSegments("roads", roads, roadWidths, roadUVs)

            // ------- HOUSES -------
            // These are used for revealing the houses one by one. They will control
            // the .alpha of the quad.
            val uv0 = houses.map { house ->
                Rectangle.fromCenter(
                    house.start.map(
                        mapBounds.corner, mapBounds.corner + mapBounds.dimensions,
                        Vector2.ZERO, Vector2.ONE
                    ), 0.0, 0.0
                )
            }
            // These are used for texture mapping.
            val textureCols = 5
            val textureRows = 5
            val uv1 = houses.map {
                // col biased exponentially towards 0
                val col = (Random.double0() * Random.double0() * textureCols).toInt()
                val row = Random.int0(textureRows)
                Rectangle(
                    col / textureCols.toDouble(),
                    row / textureRows.toDouble(),
                    1.0 / textureCols,
                    1.0 / textureRows
                )
            }
            collada.addLineSegments("houses", houses, houseWidths, uv0, uv1)

            collada.save(File("/tmp/cities/city${saveCounter++}.dae"))
//            saveFileDialog(supportedExtensions = listOf("dae")) { f ->
//                collada.save(f)
//                println("saved to $f")
//            }
        }

        extend(Screenshots())
        extend {
            drawer.run {
                clear(ColorRGBa.WHITE)
                translate(bounds.center)
                contours(contours)
                lineSegments(innerCitySegments)
                fill = ColorRGBa.GRAY
                stroke = null
                contours(houseContours)
            }
        }
        keyboard.keyDown.listen { keyEvent ->
            when (keyEvent.name) {
                "n" -> generate()
                "s" -> save()
            }

        }
    }
}

private fun Random.sign(): Double {
    return if (bool()) 1.0 else -1.0
}
