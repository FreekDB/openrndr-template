package aBeLibs.random

import org.openrndr.extra.noise.Random
import java.io.File
import java.io.FileFilter
import kotlin.math.sign

/**
 * Usage: listOf('a', 'b', 'c').pickWeighted(listOf(0.1, 0.6, 0.3)) ---> 'b' (60% chance)
 */
fun <E> Collection<E>.pickWeighted(weights: Collection<Double>): E {
    if (size != weights.size) {
        error("pickWeighted() requires two collections with the same number of elements")
    }
    val rnd = Random.double0(weights.sum())
    var sum = 0.0
    val index = weights.indexOfFirst { sum += it; sum > rnd }
    return toList()[index]
}

/**
 * Get a random double between -1 and +1, weighted towards 0
 */
@Suppress("unused")
fun Random.signedSquared(): Double {
    val r = Math.random()
    return r * r * (double0() - 0.5).sign
}

/**
 * Returns the absolute path of a random file
 * from the requested folder
 */
fun Random.file(path: String, ext: String = "jpg"): String {
    val files = File(path).listFiles(
        FileFilter { it.extension.equals(ext, ignoreCase = true) }
    )?.toList()
    return if (files == null)
        "data/images/pm5544.png"
    else
        pick(files).absolutePath
}

/**
 * Testing this random infix. Not sure I want to keep it.
 * 0.0 rnd 1.0
 */
infix fun Double.rnd(max: Double) = Random.double(this, max)

/**
 * Testing this random infix. Not sure I want to keep it.
 * 0 rnd 6
 */
infix fun Int.rnd(max: Int) = Random.int(this, max)

/**
 * Returns 1.0 or -1.0
 */
fun Random.sign() = if (bool()) 1.0 else -1.0
