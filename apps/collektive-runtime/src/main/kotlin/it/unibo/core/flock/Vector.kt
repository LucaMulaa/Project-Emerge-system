package it.unibo.core.flock

import kotlin.math.sqrt

/** A 2D vector, in metres (positions) or metres per second (velocities).
 *
 * @property x the x component.
 * @property y the y component.
 */
data class Vector(val x: Double, val y: Double) {
    /** The length of this vector. */
    val length: Double
        get() = sqrt(x * x + y * y)

    /** The sum of this vector and [other]. */
    operator fun plus(other: Vector): Vector = Vector(x + other.x, y + other.y)

    /** The difference between this vector and [other]. */
    operator fun minus(other: Vector): Vector = Vector(x - other.x, y - other.y)

    /** This vector multiplied by [num]. */
    operator fun times(num: Double): Vector = Vector(x * num, y * num)

    /** This vector divided by [num]. */
    operator fun div(num: Double): Vector = Vector(x / num, y / num)

    /** This vector, but with length 1 (or [ZERO] if it has no length). */
    fun normalized(): Vector = if (length == 0.0) ZERO else Vector(x / length, y / length)

    /** This vector, shortened to [maxLength] if longer, keeping its direction. */
    fun limitedTo(maxLength: Double): Vector {
        if (length > maxLength) {
            return normalized() * maxLength
        } else {
            return this
        }
    }

    /** Constant of vector. */
    companion object {
        /** The vector of length 0. */
        val ZERO = Vector(0.0, 0.0)
    }
}
