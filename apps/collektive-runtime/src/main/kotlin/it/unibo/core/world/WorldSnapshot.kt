package it.unibo.core.world

/**
 * A frozen copy of the [PerceivedWorld]: updates that arrive later do not change it.
 *
 * @property poses the pose of every active robot
 * @property neighbors the neighbours of every active robot, only among active robots
 */
data class WorldSnapshot(val poses: Map<String, RobotPose>, val neighbors: Map<String, Set<String>>) {
    /** The active robots, that is the ones with a pose in this snapshot. */
    val robots: Set<String>
        get() = poses.keys

    /** The neighbours of robot [id], or none if it is not in this snapshot. */
    fun neighborsOf(id: String): Set<String> = neighbors[id].orEmpty()
}
