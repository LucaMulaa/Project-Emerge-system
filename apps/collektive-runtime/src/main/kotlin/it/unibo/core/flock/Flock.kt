package it.unibo.core.flock

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.neighboring
import it.unibo.collektive.aggregate.values
import it.unibo.core.flock.Vector.Companion.ZERO
import it.unibo.core.robot.Point
import it.unibo.core.robot.bodyHeading
import it.unibo.core.world.RobotPose
import kotlin.math.cos
import kotlin.math.sin

private const val SEPARATION_DISTANCE = 0.5
private const val WAYPOINT_RADIUS = 0.5
private const val COHESION_WEIGHT = 0.6
private const val SEPARATION_WEIGHT = 1.5
private const val ALIGNMENT_WEIGHT = 0.2
private const val MIGRATION_WEIGHT = 0.8
private const val MAX_SPEED = 1.0

private val RobotPose.position: Vector get() = Vector(x, y)

private val RobotPose.direction: Vector get() = Vector(cos(bodyHeading), sin(bodyHeading))

/**
 * The velocity of robot [me] in a flock: it goes towards [waypoint], stays close to its [neighbors],
 * keeps away from the ones that are too close and faces the same way as them.
 */
fun flockVelocity(me: RobotPose, neighbors: List<RobotPose>, waypoint: Point): Vector {
    val toWaypoint = Vector(waypoint.x, waypoint.y) - me.position
    val migration = if (toWaypoint.length < WAYPOINT_RADIUS) ZERO else toWaypoint.normalized()
    if (neighbors.isEmpty()) {
        return (migration * MIGRATION_WEIGHT).limitedTo(MAX_SPEED)
    }

    val center = neighbors.map { it.position }.reduce(Vector::plus) / neighbors.size.toDouble()
    val cohesion = center - me.position

    val separation = neighbors
        .map { me.position - it.position }
        .filter { it.length < SEPARATION_DISTANCE }
        .map { away -> away.normalized() * (1 - away.length / SEPARATION_DISTANCE) }
        .fold(ZERO, Vector::plus)

    val alignment = neighbors.map { it.direction }.reduce(Vector::plus) / neighbors.size.toDouble()

    return (
        cohesion * COHESION_WEIGHT +
            separation * SEPARATION_WEIGHT +
            alignment * ALIGNMENT_WEIGHT +
            migration * MIGRATION_WEIGHT
        ).limitedTo(MAX_SPEED)
}

/** Runs the flock for this robot: it shares its [pose] with the neighbours and returns its velocity. */
fun Aggregate<String>.flock(pose: RobotPose, waypoint: Point): Vector {
    val neighbors = neighboring(pose).neighbors.values.list
    return flockVelocity(pose, neighbors, waypoint)
}
