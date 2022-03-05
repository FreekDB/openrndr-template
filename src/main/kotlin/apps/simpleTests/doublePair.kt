/**
 * id: a97f5084-9530-430a-8dc2-d67f79e35404
 * description: New sketch
 * tags: #new
 */
package apps.simpleTests

data class DoublePair(private var _a: Double) {
    private var _b: Double = _a * 2
    var first: Double
        get() = _a
        set(value) {
            _a = value
            _b = value * 2
        }
    var second: Double
        get() = _b
        set(value) {
            _a = value / 2
            _b = value
        }
}

fun main() {
    val x = DoublePair(3.0)
    println("${x.first} ${x.second}")
    x.first = 2.0
    println("${x.first} ${x.second}")
    x.second = 11.0
    println("${x.first} ${x.second}")
}
