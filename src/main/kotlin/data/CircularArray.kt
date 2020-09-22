package data

import org.openrndr.math.IntVector2
import java.util.concurrent.atomic.AtomicInteger

/**
 * A circular array that acts as a fixed size history.
 * Put values on to it replacing the oldest value.
 */
class CircularArray<T>(val size: Int, def: T) : Iterable<T> {
    private val values = MutableList(size) { def }
    private var idx = 0

    /**
     * Put a new value into the circular array
     * @param value to store
     */
    fun put(value: T) {
        idx = (idx + 1) % values.size
        values[idx] = value
    }

    /**
     * Get a value from the circular array. With no args returns oldest value
     * @param index index. 0 means most recent value, 1 is the previous one, etc
     * @return the requested value
     */
    operator fun get(index: Int): T {
        return values[(((idx + index) % values.size) + values.size) % values.size]
    }

    /**
     * Enables methods like .forEach, .all, .none, etc
     */
    override fun iterator(): Iterator<T> = object : Iterator<T> {
        private val index: AtomicInteger = AtomicInteger(idx)
        override fun hasNext(): Boolean = index.get() < idx + size
        override fun next(): T = get(index.getAndIncrement())
    }
}

fun main() {
    val items = CircularArray(4, 0.0)
    items.put(1.0)
    items.put(2.0)
    items.put(3.0)
    items.put(4.0)
    items.put(5.0)
    println("size: " + items.size)
    println("newest: " + items[0])
    println("previous: " + items[-1])
    println("oldest: " + items[1])
    println("bounds check: " + items[1000])
    println("bounds check: " + items[-1000])

    items.forEach {
        println("All items: $it")
    }
}