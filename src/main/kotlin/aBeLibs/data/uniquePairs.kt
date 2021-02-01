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

/*
// This looks nicer but doesn't work for my purposes because
// Pair(4, 8) != Pair(8, 4)
// but
// setOf(4, 8) == setOf(8, 4)
fun <E> Collection<E>.uniquePairs(): MutableSet<Pair<E, E>> {
    val result: MutableSet<Pair<E, E>> = LinkedHashSet()
    this.forEach { a ->
        this.forEach { b ->
            if(a != b) {
                result.add(Pair(a, b))
            }
        }
    }
    return result
}
*/