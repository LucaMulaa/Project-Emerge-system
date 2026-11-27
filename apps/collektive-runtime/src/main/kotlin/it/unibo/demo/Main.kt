package it.unibo.demo

import it.nicolasfarabegoli.mktt.MkttClient
import it.unibo.core.aggregate.AggregateOrchestrator
import it.unibo.core.aggregate.countRobotsPerIsland
import it.unibo.core.loop.updateLoop
import it.unibo.core.world.WorldSnapshot
import it.unibo.mqtt.MqttAggregateActuator
import it.unibo.mqtt.MqttEnvironmentProvider
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.runBlocking

private const val BROKER_PORT = 1883
private const val ROUNDS_PER_LOG = 10
private val ROUND_TIME = 100.milliseconds

/**
 * Counts the robots of the Emerge simulator with Collektive and prints the result.
 */
fun main() = runBlocking {
    val client = MkttClient {
        brokerUrl = "localhost"
        port = BROKER_PORT
    }
    client.connect()
    val provider = MqttEnvironmentProvider(client)
    provider.start(this)
    var current = WorldSnapshot(emptyMap(), emptyMap())
    val orchestrator = AggregateOrchestrator(
        neighborsOf = { id -> current.neighborsOf(id) },
        program = { countRobotsPerIsland() },
    )
    var round = 0
    updateLoop(
        period = ROUND_TIME,
        provider = provider,
        tick = { world ->
            current = world
            orchestrator.tick(world.robots)
        },
        actuator = MqttAggregateActuator(client, program = "counter"),
        boundary = { world ->
            if (round % ROUNDS_PER_LOG == 0) println("round $round: ${world.robots.size} robots")
            round++
        },
    )
}
