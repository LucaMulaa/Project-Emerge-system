package it.unibo.core.world

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

private val DEFAULT_POSE_TTL = 350.milliseconds

/**
 * The pose of one robot, as published on `/pose/<id>`.
 *
 * @property x the position along the x axis, in metres
 * @property y the position along the y axis, in metres
 * @property heading the direction the robot faces, in radians (0 = along the y axis)
 */
@Serializable
data class RobotPose(
    @SerialName("x_m") val x: Double,
    @SerialName("y_m") val y: Double,
    @SerialName("heading_rad") val heading: Double,
)

/**
 * What the runtime knows about the robots: the last pose of each one and its neighbours.
 * A pose older than [poseTtl] is forgotten, so a robot that is no longer seen leaves the world.
 */
class PerceivedWorld(
    private val timeSource: TimeSource = TimeSource.Monotonic,
    private val poseTtl: Duration = DEFAULT_POSE_TTL,
) {
    private val poses = mutableMapOf<String, Pair<RobotPose, TimeMark>>()
    private val neighbors = mutableMapOf<String, Set<String>>()

    /** Stores the latest [pose] of robot [id], together with the moment it arrived. */
    fun updatePose(id: String, pose: RobotPose) {
        poses[id] = pose to timeSource.markNow()
    }

    /** Stores the neighbours of robot [id]. */
    fun updateNeighbors(id: String, ids: Set<String>) {
        neighbors[id] = ids
    }

    /** The robots with a recent pose. Older poses are dropped. */
    fun activeRobots(): Set<String> {
        poses.entries.removeAll { it.value.second.elapsedNow() > poseTtl }
        return poses.keys.toSet()
    }

    /** The last pose of robot [id], or null if it has none. */
    fun poseOf(id: String): RobotPose? = poses[id]?.first

    /** The neighbours of robot [id], as published on `/neighbors/<id>`. */
    fun neighborsOf(id: String): Set<String> = neighbors[id].orEmpty()

    /**
     * Takes a [WorldSnapshot] of the world as it is now: only the robots with a recent pose,
     * and their neighbours among them. Older poses are dropped.
     */
    fun snapshot(): WorldSnapshot {
        val active: Set<String> = activeRobots()
        val currentPoses: Map<String, RobotPose> = poses.mapValues { (_, value) -> value.first }
        val currentNeighbors: Map<String, Set<String>> = neighbors
            .filterKeys { id -> id in active }
            .mapValues { (_, ids) -> ids intersect active }
        return WorldSnapshot(currentPoses, currentNeighbors)
    }
}
