@file:Suppress("MemberVisibilityCanBePrivate")

package axi

import aBeLibs.extensions.Handwritten
import aBeLibs.math.angleDiff
import org.openrndr.KEY_ENTER
import org.openrndr.KEY_ESCAPE
import org.openrndr.KEY_INSERT
import org.openrndr.applicationSynchronous
import org.openrndr.color.ColorRGBa
import org.openrndr.dialogs.saveFileDialog
import org.openrndr.draw.Drawer
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.Random
import org.openrndr.math.Vector2
import org.openrndr.math.map
import org.openrndr.shape.CompositionDrawer
import org.openrndr.shape.ShapeContour
import org.openrndr.svg.writeSVG
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.sin
import kotlin.system.exitProcess

/**
 * Gravitational rotation
 */

class Thing(drawer: Drawer) {
    /*
    private val numPoints = 1 + 6
    private val sep = drawer.height * 0.3
    private val waypoints = List(numPoints) {
        drawer.bounds.center + Polar(
            it * 60.0 + 30.0,
            if (it == 0) 0.0 else sep
        ).cartesian
    }
    private val waypointsNext = listOf(
            listOf(1, 3, 5), listOf(0, 2, 6), listOf(1, 3),
            listOf(0, 4, 2), listOf(3, 5), listOf(0, 6, 4), listOf(5, 1)
//        listOf(2, 6), listOf(2), listOf(1, 0), listOf(4), listOf(3, 5), listOf(4, 6), listOf(5, 0)
//        listOf(2, 4, 6), listOf(2), listOf(0, 1, 3), listOf(2), listOf(0), listOf(6), listOf(0, 5)
//        listOf(2, 4, 6), listOf(2), listOf(0, 1), listOf(4), listOf(0, 3), listOf(6), listOf(0, 5) // spiral
    )
     */

    private val sep = drawer.height * 0.2
    private val waypoints = mutableListOf<Vector2>()

    init {
        for (x in -3 until 3) {
            for (y in -2 until 3) {
                val off = if (y % 2 == 0) 0.8 else 0.3
                val p = Vector2(x * 1.0 + off, y * 0.85)
                if (Vector2.ZERO.distanceTo(p) < 2.3) {
                    waypoints.add(p * sep + drawer.bounds.position(0.6, 0.5))
                }
            }
        }
    }

    private val numPoints = waypoints.size
    private var waypointsNext: List<List<Int>>

    init {
        do {
            waypointsNext = waypoints.map { current ->
                waypoints.indices.filter { other ->
                    val d = current.distanceTo(waypoints[other])
                    d > 0.1 && d < sep * 1.1
                }.shuffled().take(if (Random.bool(0.5)) 3 else 2)
            }
        } while(!waypointsNext.flatten().sorted().toSet().containsAll(
                        (0 until numPoints).toList()))
    }

    private val maxSpeed = 0.02
    private var velocity = Vector2.ZERO
    private var acceleration = Vector2.ZERO
    private var distanceToOrbit = 0.0
    private var radius = MutableList(numPoints) { 20.0 }
    private var waypointId = 0
    private var wayPointIdNext = 1
    var location = waypoints[waypointId] + Vector2(radius[waypointId], 0.0)
        private set
    private val history = mutableListOf<Vector2>()
    private var turns = 0.0
    private var turnsExpected = 0
    private var angleDiff = 0.0
    private var angleDiffLast = 0.0

    init {
        nextWaypoint()
    }

    fun applyForce(force: Vector2) {
        acceleration += force
    }

    private fun drag() {
        val speed = velocity.length
        val dragMagnitude = 0.1 * speed * speed
        val dragForce = -velocity.normalized * dragMagnitude
        applyForce(dragForce)
    }

    private fun orbit(targets: List<Vector2>) {
        targets.forEachIndexed { id, target ->
            val mult = if (id == waypointId) 1.0 else 10.0
            val orbitForce = (location - target).normalized.rotate(90.0)
            val d = max(1.0, location.distanceTo(target) - radius[id]) * mult
            applyForce(orbitForce / d)
        }
    }

    private fun attractToRadius(target: Vector2, radius: Double) {
        val closestOrbitPoint = target + (location - target).normalized * radius
        distanceToOrbit = location.distanceTo(closestOrbitPoint)
        var attractForce = (closestOrbitPoint - location).normalized
        attractForce *= if (distanceToOrbit < radius * 2) {
            map(0.0, radius * 2, 0.0, maxSpeed, distanceToOrbit)
        } else {
            maxSpeed
        }
        applyForce(attractForce)
    }

    fun update(frm: Double) {
        val radiusIncStart = 0.003
        val radiusIncEnd = 0.001

        val locationPrev = location.copy()
        velocity += acceleration
        location += velocity
        acceleration = Vector2.ZERO

        val a = waypoints[waypointId] - locationPrev
        val b = waypoints[waypointId] - location
        val aRad = atan2(a.y, a.x)
        val bRad = atan2(b.y, b.x)
        turns += angleDiff(Math.toDegrees(bRad), Math.toDegrees(aRad))

        radius[waypointId] += //map(0.0, 45000.0, radiusIncStart, radiusIncEnd, history.size.toDouble(), true)
                map(radius[waypointId] * 1.1, radius[waypointId], 0.0, 0.1 / radius[waypointId], distanceToOrbit, true)

        attractToRadius(waypoints[waypointId], radius[waypointId] * (1 + 0.5 * sin(bRad * 6)))
        orbit(waypoints)
        drag()

        if (frm > 50.0) {
            if (history.size == 0 || history.last().distanceTo(location) > 1.0) {
                history.add(location)
            }
        }

        val dir = Math.toDegrees(atan2(velocity.y, velocity.x))
        val p = waypoints[wayPointIdNext] - location
        val dirToNext = Math.toDegrees(atan2(p.y, p.x))

        angleDiffLast = angleDiff
        angleDiff = angleDiff(dir, dirToNext)
        val angleHeading = 25 // 0 = points towards next, otherwise deviation

        if (turns > turnsExpected && angleDiff > angleHeading && angleDiffLast <= angleHeading) {
            nextWaypoint()
        }
        if (velocity.length < 0.001) {
            nextWaypoint()
        }

    }

    fun nextWaypoint() {
        turns = 0.0
        turnsExpected = 360
        waypointId = wayPointIdNext
        wayPointIdNext = Random.pick(waypointsNext[waypointId])
        if (Random.bool(0.05)) {
            radius[waypointId] += Random.double(10.0, 30.0)
        }
    }

    fun draw(drawer: Drawer) {
        drawer.run {
            stroke = ColorRGBa.GRAY
            circles(waypoints, 2.0)
            stroke = ColorRGBa.RED
            circle(location, 6.0)
            circle(waypoints[waypointId], 3.0)
            lineSegment(location, waypoints[waypointId])
            lineSegment(location, waypoints[wayPointIdNext])
            fill = ColorRGBa.BLACK

            text("%.1f".format(turns), location + 30.0)
            text(history.size.toString(), 50.0, 50.0)
        }
    }

    fun populateHandwritten(w: Handwritten) {
        waypoints.forEachIndexed { i, p ->
            w.add(i.toString(), p - Vector2(5.0, 12.0))
        }
        waypointsNext.forEachIndexed { i, ids ->
            val str = "$i => (" + ids.joinToString(", ") + ")"
            w.add(str, Vector2(20.0, 50.0 + i * 40))
        }
    }

    fun contour(): ShapeContour = ShapeContour.fromPoints(history, false)
}

// ----------------------------------------------------------------------------

fun main() = applicationSynchronous {
    configure {
        width = 1500
        height = 1200
    }
    program {
        Random.seed = System.currentTimeMillis().toString()

        val handwritten = Handwritten().apply {
            scale = 2.0
        }
        val thing = Thing(drawer)

        thing.populateHandwritten(handwritten)

        fun exportSVG() {
            val svg = CompositionDrawer()
            svg.fill = null
            svg.stroke = ColorRGBa.BLACK
            svg.contour(thing.contour())
            handwritten.drawToSVG(svg)
            saveFileDialog(supportedExtensions = listOf("svg")) {
                it.writeText(writeSVG(svg.composition))
            }
        }

        extend(handwritten)
        extend(Screenshots())
        extend {
            for (i in 0..50) {
                thing.update(frameCount * 1.0)
            }

            drawer.run {
                clear(ColorRGBa.WHITE)
                contour(thing.contour())
                fill = null
                handwritten.draw(this)
                stroke = ColorRGBa.RED
                thing.draw(this)
            }
        }
        keyboard.keyDown.listen {
            when (it.key) {
                KEY_ESCAPE -> exitProcess(0)
                KEY_ENTER -> thing.nextWaypoint()
                KEY_INSERT -> exportSVG()
            }
        }
    }
}
