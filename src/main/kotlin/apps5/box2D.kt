package apps5

import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.common.Transform
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.*
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolated
import org.openrndr.math.Matrix44
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import org.openrndr.shape.ShapeContour
import kotlin.random.Random

/**
 * id: d456399b-d436-4bdb-8e69-1c3fe5d1e101
 * description: Code shared by Edwin in Slack
 * Interactive Box2D simulation, dropping square collections and letting gravity do its thing.
 * tags: #box2D #physics
 */

fun main() = application {
    configure {
        width = 600
        height = 900
    }

    program {
        val world = World(Vec2(0.0f, .81f))
        val bodies = mutableListOf<Body>()
        val simScale = 100.0

        fun addStaticBox(rect: Rectangle) {
            val fixtureDef = FixtureDef().apply {
                shape = rect.contour.polygonShape()
                density = 1.0f
                friction = 0.01f
                restitution = 0.1f

            }
            val bodyDef = BodyDef().apply {
                type = BodyType.STATIC
                position.set(Vec2(0.0f, 0.0f))

            }
            val body = world.createBody(bodyDef)
            body.createFixture(fixtureDef)
            bodies.add(body)
        }

        val box = Rectangle.fromCenter(Vector2(0.0, 0.0), 10.0, 10.0).contour
        fun addBox() {
            val fixtureDef = FixtureDef().apply {
                shape = box.polygonShape(simScale)
                density = 1.0f
                friction = 0.3f
                restitution = 0.0f

            }
            val bodyDef = BodyDef().apply {
                type = BodyType.DYNAMIC
                position.set(
                    Vec2(
                        320.0f + Random.nextFloat() * 100.0f - 50.0f,
                        240.0f + Random.nextFloat() * 100.0f - 50.0f
                    ).mul((1 / simScale).toFloat())
                )
                this.angle = Random.nextDouble(-Math.PI, Math.PI).toFloat()
            }
            val body = world.createBody(bodyDef)
            body.createFixture(fixtureDef)

            body.linearVelocity = Vec2(
                Random.nextFloat() * 50.0f - 25.0f,
                Random.nextFloat() * 50.0f - 25.0f
            ).mul((1 / simScale).toFloat())
            body.angularVelocity =
                (Random.nextFloat() * 100.0f - 50.0f) / 100.0f
            bodies.add(body)
        }

        mouse.buttonDown.listen {
            repeat(100) {
                addBox()
            }
        }

        addStaticBox(drawer.bounds.sub(0.0, 0.99, 1.0, 1.0))
        addStaticBox(drawer.bounds.sub(0.0, 0.0, 0.01, 1.0))
        addStaticBox(drawer.bounds.sub(0.99, 0.0, 1.0, 1.0))

        extend {
            world.gravity = Vec2(0.0f, 90.81f)
            world.step(1.0f / 200.0f, 100, 100)
            drawer.apply {
                clear(ColorRGBa.RED)
                strokeWeight = 0.1
                fill = ColorRGBa.BLACK
                stroke = ColorRGBa.RED
            }

            bodies.forEach { body ->
                drawer.isolated {
                    model = body.transform.matrix44()
                    contour(box)
                }
            }
        }
    }
}

// https://www.youtube.com/embed/ZKJC2cloIqc
// https://github.com/OskarVeerhoek/YouTube-tutorials/blob/master/src/episode_31/PhysicsDemo.java

fun ShapeContour.polygonShape(scale: Double = 100.0): PolygonShape {
    val linear = sampleLinear()
    val vertices = linear.segments.map { it.start }
        .map { Vec2((it.x / scale).toFloat(), (it.y / scale).toFloat()) }
        .toTypedArray()
    return PolygonShape().apply {
        set(vertices, vertices.size)
    }
}

fun Transform.matrix44(): Matrix44 {
    val x = Vec2()
    val y = Vec2()
    this.q.getXAxis(x)
    this.q.getYAxis(y)

    return Matrix44(
        x.x * 1.0, y.x * 1.0, 0.0, p.x * 100.0,
        x.y * 1.0, y.y * 1.0, 0.0, p.y * 100.0,
        0.0, 0.0, 1.0, 0.0,
        0.0, 0.0, 0.0, 1.0
    )
}

//const val k = 30.0
//
//private val Vec2.vector2: Vector2
//    get() = Vector2(x * k, y * k)
//
//private val Vector2.vec2: Vec2
//    get() = Vec2((x / k).toFloat(), (y / k).toFloat())
//
//class KBox2D(
//    world: World, val drawer: Drawer, w: Double, h: Double
//) : BodyDef() {
//    private val body = world.createBody(this)
//    init {
//        position.set(drawer.bounds.center.vec2)
//        type = BodyType.DYNAMIC
//        body.createFixture(FixtureDef().apply {
//            density = 1f
//            shape = PolygonShape().apply {
//                setAsBox(w.toFloat(), h.toFloat())
//            }
//        })
//    }
//
//    private val rect = Rectangle.fromCenter(Vector2.ZERO, w * 30, h * 30)
//    fun draw() {
//        drawer.isolated {
//            translate(position.vector2)
//            rotate(angle.toDouble().asDegrees)
//            rectangle(rect)
//        }
//    }
//}
//
//fun main() = application {
//    program {
//        val gravity = Vec2(0f, 10f)
//        val world = World(gravity)
//        val bodies = listOf(
//            KBox2D(world, drawer, 1.0, 0.75)
//        )
//
//        extend {
//            world.step(1 / 60f, 8, 3)
//            bodies.forEach { it.draw() }
//        }
//    }
//}
