package random

import org.openrndr.extra.noise.Random

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
