package anim

import org.openrndr.animatable.easing.Easer
import org.openrndr.animatable.easing.Easing
import org.openrndr.math.mix

/*
 * Defines a timed envelope. points[] are expected as:
 * {value, time, value, time, value ...}
 * Set repetitions to -1 to loop.
 * Q: If looping, is there a way to redefine the envelope?
 *
 * initialize(new Double[]{currVal, dur_ms, targetVal}, 1);
 *
 * TODO: change to use multiple lists
 *  val sz = Envelope(listOf(40.0, 40.0, 60.0, 60.0, 40.0),
 *    listOf(400.0, 300.0, 200.0, 300.0),
 *    listOf(Easing.QuadIn, Easing.QuadOut), -1)
 * For durations and easing functions use modulo to wrap around the number of items
 * TODO: allow other data types:
 *   val sz = Envelope(listOf(Vector2.ZERO, Vector2(100.0, 500.0), Vector2(200.0, 200.0)),

 */
internal class Envelope(private var currVal: Double = 0.0) {
    private var done = true

    private var points = mutableListOf<Double>()
    private var segmentId = 0

    private var values = mutableListOf<Double>()
    private var valueId = 0
    private var times = mutableListOf<Int>()
    private var timeId = 0
    private var easings = mutableListOf<Easer>()
    private var easingId = 0

    private var repetitions = 0
    private var startTime = 0L
    private var endTime = 0L
    private var startVal = 0.0
    private var endVal = 0.0

    constructor(vararg points: Double, repetitions: Int = 1) : this(points.first()) {
        initialize(*points, repetitions = repetitions)
    }

    companion object {
        fun tick(vararg envelopes: Envelope) = envelopes.forEach { it.tick() }
    }

    fun initialize(vararg points: Double, repetitions: Int = 1, force: Boolean = false) {
        if (!done && !force) {
            // do nothing if still animating
            return
        }
        if (points.size % 2 == 0) {
            System.err.println(points)
            System.err.println(
                "${points.size} points sent to an Envelope, but it should be an even number!"
            )
        } else {
            this.repetitions = repetitions
            this.points = points.toMutableList()
            done = false
            segmentId = 0
            start()
        }
    }

    private fun start() {
        startTime = System.currentTimeMillis()
        endTime = startTime + points[segmentId + 1].toLong()
        startVal = points[segmentId]
        currVal = startVal
        endVal = points[segmentId + 2]
    }

    /**
     * Animate towards targetVal in dur_ms with a delay of delay_ms
     * Skip dur_ms for instant teleporting.
     *
     * @param targetVal
     * @param dur_ms
     * @param delay_ms
     */
    fun animateTo(targetVal: Double, dur_ms: Double = 0.0, delay_ms: Double = 0.0) {
        done = true
        if (delay_ms > 0) {
            initialize(currVal, delay_ms, currVal, dur_ms, targetVal)
        } else {
            initialize(currVal, dur_ms, targetVal)
        }
    }

    /**
     * Append a new segment to points
     *
     * @param targetVal The desired new target value
     * @param dur_ms    The time it should take from the current
     * value to the target value.
     */
    fun append(targetVal: Double, dur_ms: Double) {
        if (done) {
            animateTo(targetVal, dur_ms)
        } else {
            points.add(dur_ms)
            points.add(targetVal)
        }
    }

    /**
     * Go from currVal to targetVal (in dur1_ms) and back to currVal (in dur2_ms)
     *
     * @param dur1_ms
     * @param targetVal
     * @param dur2_ms
     */
    fun boomerang(dur1_ms: Double, targetVal: Double, dur2_ms: Double) {
        initialize(currVal, dur1_ms, targetVal, dur2_ms, currVal)
    }

    /**
     * Updates envelope
     *
     */
    fun tick() {
        if (done) {
            return
        }
        val now = System.currentTimeMillis()
        if (now > endTime) {
            segmentId += 2
            if (segmentId > points.size - 3) {
                if (--repetitions != 0) {
                    segmentId = 0
                } else {
                    done = true
                    return
                }
            }
            start()
        }
        val t = (now - startTime) / (endTime - startTime).toDouble()
        val eased = Easing.QuadInOut.easer.ease(t, 0.0, 1.0, 1.0)
        currVal = mix(startVal, endVal, eased) // could use VectorN here
    }

    override fun toString(): String {
        return """
            points: $points
            segmentId: $segmentId
            repetitions: $repetitions
            """.trimIndent()
    }

    fun toDouble(): Double = currVal

    operator fun times(d: Double): Double = currVal * d
}
