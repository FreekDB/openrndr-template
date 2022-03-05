package apps2.simpleTests

import org.openrndr.KEY_ENTER
import org.openrndr.application

import org.openrndr.math.Vector2
import kotlin.system.measureNanoTime

/**
 * id: 2b49bc9b-d4cf-4e55-8627-a732dbbb112f
 * description: New sketch
 * tags: #new
 */

// the initial number of points on each curve
const val numPoints = 50

// how many times to repeat the test
const val numRepetitions = 2_000_000 / numPoints

// number of iterations for the chaikinSmooth algorithm
const val numIterations = 3

tailrec fun chaikinArrayList(
    polyline: List<Vector2>,
    iterations: Int = 1,
    closed: Boolean = false,
    bias: Double = 0.25
): List<Vector2> {
    if (iterations <= 0 || polyline.size < 2) {
        return polyline
    }

    val biasInv = 1 - bias
    val result = ArrayList<Vector2>(polyline.size * 2)
    if (closed) {
        val sz = polyline.size - 1
        for (i in 0 until sz) {
            val p0 = polyline[i]
            val p1 = polyline[if (i + 1 == sz) 0 else i + 1]

            val (p0x, p0y) = p0
            val (p1x, p1y) = p1

            result.apply {
                add(
                    Vector2(
                        biasInv * p0x + bias * p1x,
                        biasInv * p0y + bias * p1y
                    )
                )
                add(
                    Vector2(
                        bias * p0x + biasInv * p1x,
                        bias * p0y + biasInv * p1y
                    )
                )
            }
        }
        val p0 = polyline[sz]
        val p1 = polyline[0]

        val (p0x, p0y) = p0
        val (p1x, p1y) = p1

        result.apply {
            add(
                Vector2(
                    biasInv * p0x + bias * p1x,
                    biasInv * p0y + bias * p1y
                )
            )
            add(
                Vector2(
                    bias * p0x + biasInv * p1x,
                    bias * p0y + biasInv * p1y
                )
            )
        }


    } else {

        result.add(polyline[0].copy())
        val sz = polyline.size - 1
        for (i in 0 until sz) {
            val p0 = polyline[i]
            val p1 = polyline[i + 1]

            val (p0x, p0y) = p0
            val (p1x, p1y) = p1

            result.apply {
                add(
                    Vector2(
                        biasInv * p0x + bias * p1x,
                        biasInv * p0y + bias * p1y
                    )
                )
                add(
                    Vector2(
                        bias * p0x + biasInv * p1x,
                        bias * p0y + biasInv * p1y
                    )
                )
            }
        }
        result.add(polyline[sz].copy())
    }
    return chaikinArrayList(result, iterations - 1, closed, bias)
}

tailrec fun chaikinIdiomatic(
    polyline: List<Vector2>,
    iterations: Int = 1,
    closed: Boolean = false,
    bias: Double = 0.25
): List<Vector2> {
    if (iterations <= 0 || polyline.size < 2) {
        return polyline
    }

    val result = if (closed) {
        (if (polyline.first() == polyline.last()) polyline else
            (polyline + polyline.first())).zipWithNext { p0, p1 ->
            listOf(
                p0.mix(p1, bias),
                p0.mix(p1, 1 - bias)
            )
        }.flatten()
    } else {
        listOf(polyline.first().copy()) +
                polyline.zipWithNext { p0, p1 ->
                    listOf(
                        p0.mix(p1, bias),
                        p0.mix(p1, 1 - bias)
                    )
                }.flatten() +
                polyline.last().copy()
    }

    return chaikinIdiomatic(result, iterations - 1, closed, bias)
}

fun chaikinOriginal(
    polyline: List<Vector2>, iterations: Int = 1, closed: Boolean =
        false
): List<Vector2> {
    if (iterations <= 0) {
        return polyline
    }

    val output = mutableListOf<Vector2>()

    if (!closed && polyline.isNotEmpty()) {
        output.add(polyline.first().copy())
    }

    val count = if (closed) polyline.size else polyline.size - 1

    for (i in 0 until count) {
        val p0 = polyline[i]
        val p1 = polyline[(i + 1) % polyline.size]

        val p0x = p0.x
        val p0y = p0.y
        val p1x = p1.x
        val p1y = p1.y

        val Q = Vector2(0.75 * p0x + 0.25 * p1x, 0.75 * p0y + 0.25 * p1y)
        val R = Vector2(0.25 * p0x + 0.75 * p1x, 0.25 * p0y + 0.75 * p1y)

        output.add(Q)
        output.add(R)
    }

    if (!closed && polyline.isNotEmpty()) {
        output.add(polyline.last().copy())
    }

    return chaikinOriginal(output, iterations - 1, closed)
}

private fun makePoints(offset: Int) = List(numPoints) { n ->
    Vector2((n + offset) % 1080.0, (n + offset) % 720.0)
}

fun testOriginal(closed: Boolean, offset: Int) {
    var nPoints = 0
    val t = measureNanoTime {
        repeat(numRepetitions) { rep ->
            val points = makePoints(rep + offset)
            val smooth = chaikinOriginal(points, numIterations, closed)
            nPoints += smooth.size
        }
    }
    printResult(object {}.javaClass.enclosingMethod.name, t, nPoints, closed)
}

fun testIdiomatic(closed: Boolean, offset: Int) {
    var nPoints = 0
    val t = measureNanoTime {
        repeat(numRepetitions) { rep ->
            val points = makePoints(rep + offset)
            val smooth = chaikinIdiomatic(points, numIterations, closed)
            nPoints += smooth.size
        }
    }
    printResult(object {}.javaClass.enclosingMethod.name, t, nPoints, closed)
}

fun testArrayList(closed: Boolean, offset: Int) {
    var nPoints = 0
    val t = measureNanoTime {
        repeat(numRepetitions) { rep ->
            val points = makePoints(rep + offset)
            val smooth = chaikinArrayList(points, numIterations, closed)
            nPoints += smooth.size
        }
    }
    printResult(object {}.javaClass.enclosingMethod.name, t, nPoints, closed)
}

private fun printResult(name: String, t: Long, nPoints: Int, closed: Boolean) {
    println(
        "[$name] time: ${t / 1_000_000}ms for $nPoints points (${
            if (closed)
                "closed" else "open"
        })"
    )
}

fun main() = application {
    program {
        println(
            "Press ENTER to run tests. Second round and following ones " +
                    "give consistent results."
        )
        extend {
        }
        keyboard.keyDown.listen {
            if (it.key == KEY_ENTER) {
                val offset = frameCount
                testOriginal(true, offset)
                testOriginal(false, offset)
                testArrayList(true, offset)
                testArrayList(false, offset)
                testIdiomatic(true, offset)
                testIdiomatic(false, offset)
                println()
            }
        }
    }
}
