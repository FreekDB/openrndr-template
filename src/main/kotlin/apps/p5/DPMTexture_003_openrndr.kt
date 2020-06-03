package apps.p5

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.draw.LineCap
import org.openrndr.draw.isolated
import org.openrndr.extra.noise.Random
import org.openrndr.math.Vector2
import org.openrndr.shape.ShapeContour

// activate "orx-noise" in build.gradle.kts

fun main() = application {
    configure {
        width = 500
        height = 500
    }

    program {

        var tileSize = 50
        var angle = 0.0
        var sw = 1.0
        val theCol = ColorRGBa.WHITE
        var randSeed = "1234"

        var isRotate = true
        var isRandomTile = false
        var isOutline = false

        fun Drawer.rect(x: Double, y: Double, w: Double, h: Double) {
            rectangle(x - w / 2, y - h / 2, w, h)
        }

        fun Drawer.triangle(
            x0: Double, y0: Double, x1: Double, y1: Double,
            x2: Double, y2: Double
        ) {
            contour(
                ShapeContour.fromPoints(
                    listOf(Vector2(x0, y0), Vector2(x1, y1), Vector2(x2, y2)), true
                )
            )
        }

        fun module01(x: Double, y: Double, taille: Double, isOutline: Boolean) {
            drawer.isolated {
                stroke = if (isOutline) theCol else null
                fill = null
                translate(x, y)
                rect(0.0, 0.0, taille, taille)
            }
        }

        fun module02(x: Double, y: Double, taille: Double, isOutline: Boolean) {
            drawer.isolated {
                translate(-taille / 2, -taille / 2)
                stroke = theCol
                if (isOutline) {
                    fill = null
                    rect(x + taille / 2, y + taille / 2, taille, taille)
                }

                translate(x, y)
                lineSegment(0.0, taille / 2, taille / 2, 0.0)
                lineSegment(0.0, taille, taille, 0.0)
                for (i in 0..9) {
                    val p = taille * i / 10.0
                    lineSegment(taille - p, p, taille, p)
                }
            }
        }

        fun module03(x: Double, y: Double, taille: Double, isOutline: Boolean) {
            drawer.isolated {
                translate(-taille / 2, -taille / 2)
                stroke = theCol
                if (isOutline) {
                    fill = null
                    rect(x + taille / 2, y + taille / 2, taille, taille)
                }

                fill = theCol
                translate(x, y)
                strokeWeight = sw + 0.5
                triangle(0.0, taille, taille, taille, taille, 0.0)
                strokeWeight = sw
                for (i in 0..10) {
                    val p = taille * i / 10.0
                    lineSegment(0.0, p, taille - p, p)
                }
            }
        }

        fun module04(x: Double, y: Double, taille: Double, isOutline: Boolean) {
            drawer.isolated {
                translate(-taille / 2, -taille / 2)
                stroke = theCol
                if (isOutline) {
                    fill = null
                    rect(x + taille / 2, y + taille / 2, taille, taille)
                }

                fill = theCol
                translate(x, y)
                triangle(0.0, 0.0, taille, 0.0, 0.0, taille)
                lineSegment(0.0, taille, taille, 0.0)
                lineSegment(taille / 2, taille, taille, taille / 2)
            }
        }

        fun module05(x: Double, y: Double, taille: Double, isOutline: Boolean) {
            drawer.isolated {
                translate(-taille / 2, -taille / 2)
                stroke = theCol
                if (isOutline) {
                    fill = null
                    rect(x + taille / 2, y + taille / 2, taille, taille)
                }

                fill = null
                translate(x, y)
                for (i in 0..10) {
                    val p = taille * i / 10.0
                    lineSegment(0.0, p, taille, p)
                }
            }
        }

        fun module06(x: Double, y: Double, taille: Double, isStroke: Boolean) {
            drawer.isolated {
                stroke = if (isStroke) theCol else null
                fill = theCol
                translate(x, y)
                rect(0.0, 0.0, taille, taille)
            }
        }

        fun module07(x: Double, y: Double, taille: Double, isOutline: Boolean) {
            drawer.isolated {
                translate(-taille / 2, -taille / 2)
                stroke = theCol
                if (isOutline) {
                    fill = null
                    rect(x + taille / 2, y + taille / 2, taille, taille)
                }

                fill = null
                translate(x, y)
                lineSegment(0.0, taille / 2, taille / 2, 0.0)
                lineSegment(0.0, taille, taille, 0.0)
                lineSegment(taille / 2, taille, taille, taille / 2)
            }
        }

        fun module08(x: Double, y: Double, taille: Double, isOutline: Boolean) {
            drawer.isolated {
                translate(-taille / 2, -taille / 2)
                stroke = theCol
                if (isOutline) {
                    fill = null
                    rect(x + taille / 2, y + taille / 2, taille, taille)
                }
                fill = theCol
                strokeWeight = sw + 0.5
                translate(x, y)
                triangle(0.0, 0.0, taille, 0.0, 0.0, taille)

                strokeWeight = sw
                for (i in 0..10) {
                    val p = taille * i / 10.0
                    lineSegment(taille - p, p, taille, p)
                }
            }
        }

        val modules = listOf(
            ::module01, ::module02, ::module03, ::module04,
            ::module05, ::module06, ::module07, ::module08
        )
        var module = modules[0]

        keyboard.character.listen {
            when (it.character) {
                'r' -> randSeed = Math.random().toString()
                't' -> isRandomTile = !isRandomTile
                'a' -> isRotate = !isRotate
                'o' -> isOutline = !isOutline
                'l' -> tileSize += 25
                'm' -> if (tileSize > 50) tileSize -= 25
                'w' -> sw += 0.5
                'x' -> if (sw > 0.5) sw -= 0.5
                '1' -> module = ::module01
                '2' -> module = ::module02
                '3' -> module = ::module03
                '4' -> module = ::module04
                '5' -> module = ::module06
                '6' -> module = ::module07
            }
        }

        extend {
            drawer.background(ColorRGBa.BLACK)
            Random.seed = randSeed
            drawer.strokeWeight = sw
            drawer.lineCap = LineCap.SQUARE

            for (y in tileSize / 2 until height step tileSize) {
                for (x in tileSize / 2 until width step tileSize) {
                    angle = when (val r = Random.int0(5)) {
                        in 0..3 -> r * 90.0
                        else -> angle
                    }
                    drawer.isolated {
                        translate(x * 1.0, y * 1.0)
                        if (isRotate) {
                            rotate(angle)
                        }
                        val currentModule = if (isRandomTile) Random.pick(modules) else module
                        currentModule(0.0, 0.0, tileSize * 1.0, isOutline)
                    }
                }
            }
        }
    }
}
