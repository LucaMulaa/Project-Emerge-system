package it.unibo.demo

import it.nicolasfarabegoli.mktt.MkttClient
import it.unibo.core.aggregate.AggregateOrchestrator
import it.unibo.core.aggregate.countRobots
import it.unibo.core.world.PerceivedWorld
import it.unibo.core.world.RobotPose
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

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
    val json = Json { ignoreUnknownKeys = true }
    val world = PerceivedWorld()
    launch {
        client.subscribe("/neighbors/+").collect { message ->
            val robotId = message.topic.split("/").last()
            world.updateNeighbors(robotId, Json.decodeFromString<List<String>>(message.payloadAsString()).toSet())
        }
    }
    launch {
        client.subscribe("/pose/+").collect { message ->
            val robotId = message.topic.split("/").last()
            runCatching { json.decodeFromString<RobotPose>(message.payloadAsString()) }
                .onSuccess { pose -> world.updatePose(robotId, pose) }
                .onFailure { println("Ignored a malformed pose from $robotId: ${it.message}") }
        }
    }
    val orchestrator = AggregateOrchestrator(
        neighborsOf = world::neighborsOf,
        program = { countRobots(sink = localId == "000000") },
    )
    var round = 0
    while (true) {
        val results = orchestrator.tick(world.activeRobots())
        results.forEach { (id, value) ->
            val payload = """{"program":"counter","value":$value}"""
            client.publish("/aggregate/$id", payload.encodeToByteArray())
        }

        if (round % ROUNDS_PER_LOG == 0) {
            println("round $round: $results")
            println("  000000 is at ${world.poseOf("000000")}")
        }
        round++
        delay(ROUND_TIME)
    }
}
