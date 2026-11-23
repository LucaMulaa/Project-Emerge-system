package it.unibo.demo

import it.nicolasfarabegoli.mktt.MkttClient
import it.unibo.core.robot.Move
import it.unibo.core.robot.Stop
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

private const val BROKER_PORT = 1883
private const val SPIN_STEPS = 30
private const val SPIN_POWER = 0.3
private val SEND_PERIOD = 100.milliseconds

/** Spins robot 000000 on the spot for 3 seconds, then stops it. */
fun main() = runBlocking {
    val client = MkttClient {
        brokerUrl = "localhost"
        port = BROKER_PORT
    }
    client.connect()
    val topic = "/motors/000000"
    try {
        repeat(SPIN_STEPS) {
            client.publish(topic, Move(SPIN_POWER, -SPIN_POWER).toPayload().encodeToByteArray())
            delay(SEND_PERIOD)
        }
    } finally {
        withContext(NonCancellable) {
            client.publish(topic, Stop.toPayload().encodeToByteArray())
            client.disconnect()
        }
    }
}
