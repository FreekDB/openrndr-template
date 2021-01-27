package latest

import aBeLibs.data.uniquePairs
import org.jgrapht.Graph
import org.jgrapht.GraphTests
import org.jgrapht.Graphs

/**
 * Find cycles with 4 vertices
 */
fun <V, E> AbeCycle(graph: Graph<V, E>): Set<Set<V>> {
    GraphTests.requireUndirected(graph)
    require(!GraphTests.hasMultipleEdges(graph)) { "Graphs with multiple edges not supported" }

    val cycles: MutableSet<Set<V>> = mutableSetOf()
    val paths: MutableMap<V, MutableSet<Set<E?>>> = mutableMapOf()

    // explore every vertex
    for (root in graph.vertexSet()) {
        // we are looking for loops. A vertex with less than 2 neighbors can't form a loop.
        if (graph.degreeOf(root) < 2) {
            continue
        }
        paths.clear()
        graph.edgesOf(root).forEach { edge ->
            // direct neighbor
            val neighbor = Graphs.getOppositeVertex(graph, edge, root)
            graph.edgesOf(neighbor).forEach { edgeNext ->
                // skip edge going back to root
                if (edge != edgeNext) {
                    // neighbor of neighbor
                    val neighbor2 = Graphs.getOppositeVertex(graph, edgeNext, neighbor)
                    // add a route to get to neighbor2
                    if (neighbor2 !in paths) {
                        paths[neighbor2] = mutableSetOf()
                    }
                    // we collect all neighbors of neighbors with the route to get there
                    // if we can get to the same vertex via two different routes, we have a loop.
                    paths[neighbor2]!!.add(setOf(edge, edgeNext))
                }
            }
        }
        paths.forEach { (midVertex, routes) ->
            // make sure there's at least two ways to get to that midVertex
            // (neighbor of neighbor)
            if (routes.size >= 2) {
                // there might be more than 2 routes, so make all possible combinations
                // for example r1+r2, r1+r3, r2+r3 if we have 3 routes
                val pathPairs = routes.uniquePairs()
                // for each of those routes...
                pathPairs.forEach { pair ->
                    // pair is Set [Set[E,E], Set[E,E]]
                    val edges = pair.flatten() // 2 edges + 2 edges
                    // look at the source+target vertices of each hop.
                    // the vertex shared by both hops (intersection)
                    // is the one betwene [root] and [midVertex]
                    val v0 = listOf(
                        graph.getEdgeSource(edges[0]),
                        graph.getEdgeTarget(edges[0])
                    ).intersect(listOf(
                        graph.getEdgeSource(edges[1]),
                        graph.getEdgeTarget(edges[1])
                    )).first()
                    val v1 = listOf(
                        graph.getEdgeSource(edges[2]),
                        graph.getEdgeTarget(edges[2])
                    ).intersect(listOf(
                        graph.getEdgeSource(edges[3]),
                        graph.getEdgeTarget(edges[3])
                    )).first()
                    cycles.add(setOf(root, v0, midVertex, v1))
                }
            }
        }
    }

    return cycles
}
