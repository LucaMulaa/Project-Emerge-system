package it.unibo.mqtt

import it.nicolasfarabegoli.mktt.MkttClient
import it.unibo.core.loop.EnvironmentProvider
import it.unibo.core.world.PerceivedWorld
import it.unibo.core.world.WorldSnapshot
import it.unibo.core.world.followNeighbors
import it.unibo.core.world.followPoses
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

/**
 * Builds the world from what the robots publish on MQTT (/pose/+ and /neighbors/+).
 * Call [start] once, then every [provide] gives a snapshot of the world at that moment.
 */
class MqttEnvironmentProvider(private val client: MkttClient, private val world: PerceivedWorld = PerceivedWorld()) :
    EnvironmentProvider {
    /** Starts following poses and neighbours in [scope]. Returns the jobs, so they can be cancelled. */
    fun start(scope: CoroutineScope): List<Job> = listOf(
        scope.followPoses(client, world),
        scope.followNeighbors(client, world),
    )

    override suspend fun provide(): WorldSnapshot = world.snapshot()
}
