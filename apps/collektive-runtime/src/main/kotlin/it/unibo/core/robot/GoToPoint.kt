package it.unibo.core.robot

import it.unibo.core.world.RobotPose
import kotlin.math.IEEErem
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

private const val DRIVE_POWER = 0.8
private const val SLOWDOWN_DISTANCE = 1.0
private const val SPIN_GAIN = 0.8
private const val MAX_SPIN = 0.5
private const val STEER_GAIN = 0.5
private const val MAX_STEER = 0.3
private val SPIN_THRESHOLD = Math.toRadians(35.0)

/**
 * A point of the arena.
 * @property x the position along the x axis, in metres
 * @property y the position along the y axis, in metres
 */
data class Point(val x: Double, val y: Double)

/** Brings [angle] into [-PI, PI], so that the robot always turns the short way. */
fun normalizeAngle(angle: Double): Double = angle.IEEErem(2 * PI)

/**
 * The direction the body of the robot faces, in radians, with 0 along the x axis.
 * The simulator publishes 0 when the robot faces the y axis, so a quarter turn is added.
 */
val RobotPose.bodyHeading: Double get() = normalizeAngle(heading + PI / 2)

/**
 * The command that brings a robot closer to a target: [Stop] once arrived,
 * a spin on the spot while the target is too far to the side, otherwise a forward drive that steers.
 * @param pose where the robot is and where it faces
 * @param target the point the robot has to reach
 * @param arrivedDistance the distance, in metres, below which the robot is considered arrived
 */
fun goTo(pose: RobotPose, target: Point, arrivedDistance: Double = 0.1): MotorCommand {
    val dx = target.x - pose.x
    val dy = target.y - pose.y
    val distance = sqrt(dx * dx + dy * dy)
    val error = normalizeAngle(atan2(dy, dx) - pose.bodyHeading)

    return when {
        distance < arrivedDistance -> Stop

        abs(error) > SPIN_THRESHOLD -> {
            val turn = (SPIN_GAIN * error).coerceIn(-MAX_SPIN, MAX_SPIN)
            Move(-turn, turn)
        }

        else -> {
            val forward = DRIVE_POWER * minOf(1.0, distance / SLOWDOWN_DISTANCE)
            val turn = (STEER_GAIN * error).coerceIn(-MAX_STEER, MAX_STEER)
            Move(forward - turn, forward + turn)
        }
    }
}
