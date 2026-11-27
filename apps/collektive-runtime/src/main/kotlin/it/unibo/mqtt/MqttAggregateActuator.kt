package it.unibo.mqtt

import it.nicolasfarabegoli.mktt.MkttClient
import it.unibo.core.loop.Actuator

/** Publishes the value of [program] for each robot on /aggregate/<id>, where the dashboard reads it. */
class MqttAggregateActuator(private val client: MkttClient, private val program: String) : Actuator<Int> {
    override suspend fun act(id: String, actuation: Int) {
        val payload = """{"program":"$program","value":$actuation}"""
        client.publish("/aggregate/$id", payload.encodeToByteArray())
    }
}
