package apps2

import geometry.intersects
import geometry.noisified
import org.openrndr.*
import org.openrndr.color.ColorRGBa
import org.openrndr.color.ColorXSVa
import org.openrndr.dialogs.saveFileDialog
import org.openrndr.draw.isolated
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.Random
import org.openrndr.extra.noise.simplex
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.shape.CompositionDrawer
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.contour
import org.openrndr.svg.writeSVG
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.system.exitProcess

/**
 * 1. Create main shape - mother
 * 2. Draw normals point out from mother
 * 3. Connect two normals with a bezier curve, creating petals
 * 4. Make sure the petals don't cross with each other. Being inside each other is ok though.
 * 5. Instead of fully random locations, sometimes suggest petals slightly inside or outside
 *    other petals.
 * 6. This leads to too many lines close to each other. I could create a list of slots.
 *    Mother will have 1000 slots, for example. When we consume all slots, we are done.
 *    When figuring out which slots to use the approah would be similar to the current one:
 *    either pick two random ones with a maximum separation, or pick adjacent ones to an
 *    existing line. Basically it would be like the current usage of `used`, but with integers
 */
fun main() = application {
    configure {
        width = 15 * 60
        height = 12 * 60
    }

    program {
        var center = drawer.bounds.center
        var rotation = 0.0
        var bgcolor = ColorRGBa.PINK

        val maxNumOfHairs = 360
        var hairContours = mutableListOf<ShapeContour>()
        val hairLocations = mutableListOf<Pair<Int, Int>>()
        val motherSlots = MutableList(maxNumOfHairs) { false }

        val seed = System.currentTimeMillis().toInt()

        val mother = ShapeContour.fromPoints(List(200) {
            val a = 2 * PI * it / 200.0
            Polar(Math.toDegrees(a),
                    200.0 + 80.0 * simplex(seed, cos(a), sin(a)))
                    .cartesian +
                    Polar(Math.toDegrees(a),
                            80.0 * simplex(seed, cos(a + 1), sin(a + 1)))
                            .cartesian
        }, true)

        fun exportSVG() {
            val svg = CompositionDrawer()
            svg.translate(drawer.bounds.center)
            svg.fill = null
            svg.stroke = ColorRGBa.BLACK
            svg.contour(mother)
            svg.contours(hairContours)
            saveFileDialog(supportedExtensions = listOf("svg")) {
                it.writeText(writeSVG(svg.composition))
            }
        }

        extend(Screenshots())
        extend {
            if (hairContours.size < maxNumOfHairs - 1) {
                // figure out which slots to connect
                val jmp = (15 + seconds * 20).toInt()
                var v1: Int
                var v2: Int
                if (Random.bool(0.3) || hairContours.isEmpty()) {
                    v1 = Random.int0(maxNumOfHairs)
                    v2 = (v1 + Random.int(1, jmp)) % maxNumOfHairs
                } else {
                    val v = Random.pick(hairLocations)
                    val offset = Random.int(-50, 50)
                    v1 = (v.first + offset + maxNumOfHairs) % maxNumOfHairs
                    v2 = (v.second - offset + maxNumOfHairs) % maxNumOfHairs
                }

                if (!motherSlots[v1] && !motherSlots[v2] && v1 != v2) {
                    val normalize = 1.0 / maxNumOfHairs
                    val n1 = mother.normal(v1 * normalize)
                    val n2 = mother.normal(v2 * normalize)
                    val p1 = mother.position(v1 * normalize)
                    val p2 = mother.position(v2 * normalize)

                    val d = p1.distanceTo(p2)
                    val sharpness = Random.double(0.5, 1.0)
                    val side = Random.int0(2) * 2.0 - 1.0
                    val c = contour {
                        moveTo(p1 + n1 * side)
                        curveTo(p1 + n1 * d * sharpness * side,
                                p2 + n2 * d * sharpness * side,
                                p2 + n2 * side)
                    }
                    val hair = c.sampleLinear(0.5)
                    if (hairContours.all { it.intersects(hair) == Vector2.INFINITY }) {
                        hairContours.add(hair)
                        if (Random.bool(0.5) && side > 0) {
                            val copies = 1 + (d / 80).toInt()
                            for (i in 1..copies) {
                                hairContours.add(hair.noisified(i, false, 0.02))
                            }
                        }
                        hairLocations.add(Pair(v1, v2))
                        motherSlots[v1] = true
                        motherSlots[v2] = true
                    }
                }
            }

            drawer.isolated {
                clear(bgcolor)
                translate(center)
                rotate(rotation)
                fill = null
                stroke = ColorRGBa(0.0, 0.0, 0.0, 0.5)
                contour(mother)
                contours(hairContours)
            }
        }

        mouse.dragged.listen {
            if (mouse.pressedButtons.contains(MouseButton.LEFT)) {
                center += it.dragDisplacement
            }
            if (mouse.pressedButtons.contains(MouseButton.RIGHT)) {
                rotation += it.dragDisplacement.x
            }
        }

        keyboard.keyDown.listen {
            when (it.key) {
                KEY_ESCAPE -> exitProcess(0)
                KEY_INSERT -> exportSVG()
                KEY_ENTER -> bgcolor = ColorXSVa(Random.double0(360.0), 0.3, 0.95).toRGBa()
            }
        }
    }
}

