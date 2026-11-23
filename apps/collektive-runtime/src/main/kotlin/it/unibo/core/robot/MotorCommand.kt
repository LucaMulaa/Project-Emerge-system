package it.unibo.core.robot

/** A command for the two wheels of a robot, published on `/motors/<id>`. */
sealed interface MotorCommand {
    /** The JSON text to publish on `/motors/<id>`. */
    fun toPayload(): String
}

/**
 * Drives the two wheels of a robot.
 *
 * @property left the power of the left wheel, from -1 (backward) to 1 (forward)
 * @property right the power of the right wheel, from -1 (backward) to 1 (forward)
 */
data class Move(val left: Double, val right: Double) : MotorCommand {
    override fun toPayload(): String {
        val safeLeft = left.coerceIn(-1.0, 1.0)
        val safeRight = right.coerceIn(-1.0, 1.0)
        return """{"Move":{"left":$safeLeft,"right":$safeRight}}"""
    }
}

/** Stops both wheels of a robot. */
data object Stop : MotorCommand {
    override fun toPayload(): String = "\"Stop\""
}
