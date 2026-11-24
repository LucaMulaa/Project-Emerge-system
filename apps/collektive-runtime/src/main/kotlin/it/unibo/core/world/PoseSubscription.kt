package it.unibo.core.world

import it.nicolasfarabegoli.mktt.MkttClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

/**
 * Follows the poses that the robots publish on /pose/+ and writes them into [world].
 * Returns the [Job] of the subscription, so that it can be cancelled.
 */
fun CoroutineScope.followPoses(client: MkttClient, world: PerceivedWorld): Job = launch {
    client.subscribe("/pose/+").collect { message ->
        val robotId = message.topic.split("/").last()
        runCatching { json.decodeFromString<RobotPose>(message.payloadAsString()) }
            .onSuccess { pose -> world.updatePose(robotId, pose) }
            .onFailure { println("Ignored a malformed pose from $robotId: ${it.message}") }
    }
}
