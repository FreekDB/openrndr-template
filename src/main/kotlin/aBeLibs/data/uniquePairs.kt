package aBeLibs.data

fun <E> Collection<E>.uniquePairs(): MutableSet<Set<E>> {
    val result: MutableSet<Set<E>> = LinkedHashSet()
    this.forEach { a ->
        this.forEach { b ->
            if(a != b) {
                result.add(setOf(a, b))
            }
        }
    }
    return result
}
