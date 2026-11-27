package it.unibo.mqtt

import it.nicolasfarabegoli.mktt.MkttClient
import it.unibo.core.loop.Actuator
import it.unibo.core.robot.MotorCommand
import it.unibo.core.robot.Stop

/** Publishes a [MotorCommand] for each robot on /motors/<id>, and remembers who got one. */
class MqttMotorsActuator(private val client: MkttClient) : Actuator<MotorCommand> {
    private val moved = mutableSetOf<String>()

    override suspend fun act(id: String, actuation: MotorCommand) {
        client.publish("/motors/$id", actuation.toPayload().encodeToByteArray())
        moved += id
    }

    /** Sends [Stop] to every robot that got a command, so no robot keeps moving. */
    suspend fun stopAll() {
        moved.forEach { id -> act(id, Stop) }
    }
}
