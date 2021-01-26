package aBeLibs.data

data class Array3D<T>(private val width: Int, private val height: Int, private val depth: Int, private val defaultVal: T) {
    private val data = MutableList(width * height * depth) { defaultVal }

    operator fun get(i: Int, j: Int, k: Int): T {
        return data[i * width * height + j * depth + k]
    }

    operator fun set(i: Int, j: Int, k: Int, v: T) {
        data[i * width * height + j * depth + k] = v
    }

    fun size() {
        data.size
    }
}
