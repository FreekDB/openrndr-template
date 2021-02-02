package aBeLibs.anim

import org.openrndr.animatable.easing.Easing
import org.openrndr.math.mix

/*
 * val x = Envelope(100.0)
 * val y = Envelope(
 *   values = listOf(50.0, 200.0),
 *   times = listOf(100, 1000),
 *   easings = listOf(Easing.QuadInOut),
 *   repetitions = 3)
 * repetitions = -1 for loop
 *
 * x.animateTo(300.0, dur_ms = 500, easing = Easing.QuadInOut)
 * x.append(400.0, dur_ms = 500, easing = Easing.QuadInOut)
 * x.delay(4000)
 * x.boomerang(1000, 100.0, 1000, easing = Easing.QuadInOut)
 *
 * TODO: allow other data types:
 *   val sz = Envelope(listOf(Vector2.ZERO, Vector2(100.0, 500.0), Vector2(200.0, 200.0)),
 */
class Envelope2(private var currVal: Double = 0.0) {
    private var paused = true

    private var posId = 0
    private var values = mutableListOf<Double>()
    private var times = mutableListOf<Int>()
    private var easings = mutableListOf<Easing>()

    private var repetitions = 0
    private var time0 = 0L
    private var time1 = 0L
    private var val0 = 0.0
    private var val1 = 0.0
    private var easing = Easing.QuadInOut

    constructor(
        values: List<Double>,
        times: List<Int> = listOf(1000), // 1 second transitions by default
        easings: List<Easing> = listOf(Easing.QuadInOut),
        repetitions: Int = 1
    ) : this(values.first()) {
        initialize(values, times, easings, repetitions = repetitions)
    }

    companion object {
        fun tick(vararg envelopes: Envelope2) = envelopes.forEach { it.tick() }
    }

    fun initialize(
        values: List<Double>,
        times: List<Int> = listOf(1000),
        easings: List<Easing> = listOf(Easing.QuadInOut),
        repetitions: Int = 1,
        force: Boolean = false
    ) {
        if (!paused && !force) {
            // do nothing if still animating
            return
        }
        this.repetitions = repetitions
        this.values = values.toMutableList()
        this.times = times.toMutableList()
        this.easings = easings.toMutableList()
        paused = false
        posId = 0
        start()
    }

    private fun start() {
        time0 = System.currentTimeMillis()
        time1 = time0 + times[posId % times.size]
        val0 = values[posId % values.size]
        val1 = values[(posId + 1) % values.size]
        currVal = val0
        easing = easings[posId % easings.size]
    }

    /**
     * Animate towards targetVal in dur_ms
     * Skip dur_ms for instant teleporting.
     *
     * @param targetVal
     * @param dur_ms
     * @param easing
     */
    fun animateTo(targetVal: Double, dur_ms: Int = 1000, easing: Easing = Easing.QuadInOut) {
        paused = true
        if(dur_ms > 0) {
            initialize(
                listOf(currVal, targetVal),
                listOf(dur_ms),
                listOf(easing)
            )
        } else {
            initialize(listOf(currVal))
        }
    }

    fun addDelay(delay: Int) {
        if(delay > 0) {
            append(values.last(), delay)
        }
    }

    /**
     * Append a new animation segment
     *
     * @param targetVal The desired new target value
     * @param dur_ms    The time it should take from the current
     * value to the target value.
     */
    fun append(targetVal: Double, dur_ms: Int, easing: Easing = Easing.QuadInOut) {
        if (paused) {
            animateTo(targetVal, dur_ms)
        } else {
            values.add(targetVal)
            times.add(dur_ms)
            easings.add(easing)
        }
    }

    /**
     * Go from currVal to targetVal (in dur1_ms) and back to currVal (in dur2_ms)
     *
     * @param dur1_ms
     * @param targetVal
     * @param dur2_ms
     */
    fun boomerang(dur1_ms: Int, targetVal: Double, dur2_ms: Int, easing: Easing = Easing.QuadInOut) {
        if(paused) {
            initialize(
                listOf(currVal, targetVal, currVal),
                listOf(dur1_ms, dur2_ms),
                listOf(easing)
            )
        } else {
            val lastVal = values.last()
            append(targetVal, dur1_ms, easing)
            append(lastVal, dur2_ms, easing)
        }
    }

    /**
     * Updates envelope values based on current time in milliseconds
     */
    fun tick() {
        if (paused) {
            return
        }
        val now = System.currentTimeMillis()
        if (now > time1) {
            posId++
            if ((posId+1) % values.size == 0) {
                if (repetitions == 1) {
                    paused = true
                    return
                } else {
                    repetitions--
                }
            }
            start()
        }
        val t = (now - time0) / (time1 - time0).toDouble()
        val eased = easing.easer.ease(t, 0.0, 1.0, 1.0)
        currVal = mix(val0, val1, eased) // could use VectorN here
    }

    override fun toString(): String {
        return "values: $values (posId $posId), times: $times, easings: $easings, repetitions: $repetitions"
    }

    fun toDouble(): Double = currVal

    operator fun times(d: Double): Double = currVal * d
}
