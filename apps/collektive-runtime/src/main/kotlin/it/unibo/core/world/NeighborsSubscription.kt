package it.unibo.core.world

import it.nicolasfarabegoli.mktt.MkttClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

/**
 * Follows the neighbors that the robots publish on /neighbors/+ and writes them into [world].
 * Returns the [Job] of the subscription, so that it can be cancelled.
 */
fun CoroutineScope.followNeighbors(client: MkttClient, world: PerceivedWorld): Job = launch {
    client.subscribe("/neighbors/+").collect { message ->
        val robotId = message.topic.split("/").last()
        runCatching { Json.decodeFromString<Set<String>>(message.payloadAsString()) }
            .onSuccess { ids -> world.updateNeighbors(robotId, ids) }
            .onFailure { println("Ignored a malformed neighbors from $robotId: ${it.message}") }
    }
}
