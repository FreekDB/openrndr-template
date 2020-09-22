package apps2.city

import org.openrndr.math.Vector2
import kotlin.math.min

class City(var position: Vector2) {
    private val maxSize = 100.0
    private var shrinkDelay = 150
    var size = 0.0
    fun evolve(blobs: List<Blob>) {
        if (blobs.any { blob -> blob.position.distanceTo(position) < blobFeedsCityDistance }) {
            size = min(size + 1.0, maxSize)
            shrinkDelay = 50
            // TODO: grow roads from the city center. Forking but also merging.
            // Idea: use 8 angles to produce metro-map-like streets but also use a voronoi map
            // as an angle offset to make it less boring.
            // Roads are grown from moving particles that track the surrounding. Like branches.
            // Branches stop when too long (when getting outside the city `size`)
            // Houses grow on the sides of roads. Rectangles parallel to a road. Make sure
            // houses keep a safe distance from other houses
        } else {
            if (--shrinkDelay < 0) {
                size -= 1.0
            }
        }
    }
}