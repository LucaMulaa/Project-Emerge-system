package it.unibo.demo

import it.nicolasfarabegoli.mktt.MkttClient
import it.unibo.core.aggregate.AggregateOrchestrator
import it.unibo.core.aggregate.countRobots
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
    val neighbors = mutableMapOf<String, Set<String>>()
    launch {
        client.subscribe("/neighbors/+").collect { message ->
            val robotId = message.topic.split("/").last()
            neighbors[robotId] = Json.decodeFromString<List<String>>(message.payloadAsString()).toSet()
        }
    }
    val orchestrator = AggregateOrchestrator(
        neighborsOf = { id -> neighbors[id].orEmpty() },
        program = { countRobots(sink = localId == "000000") },
    )
    var round = 0
    while (true) {
        val results = orchestrator.tick(neighbors.keys.toSet())
        results.forEach{ (id,value)->
            val payload = """{"program":"counter","value":$value}"""
            client.publish("/aggregate/$id", payload.encodeToByteArray())
        }

        if (round % ROUNDS_PER_LOG == 0) {
            println("round $round: $results")
        }
        round++
        delay(ROUND_TIME)
    }
}
