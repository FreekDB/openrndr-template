package aBeLibs.kotlin

inline fun doubleRepeat(steps: Int, from: Double = 0.0, to: Double = 1.0,
                        action: (Double) -> Unit) {
    for (index in 0 until steps) {
        action(from + (to - from) * index / (steps - 1))
    }
}

inline fun loopRepeat(steps: Int, from: Double = 0.0, to: Double = 1.0,
                      action: (Double) -> Unit) {
    for (index in 0 until steps) {
        action(from + (to - from) * index / steps)
    }
}
