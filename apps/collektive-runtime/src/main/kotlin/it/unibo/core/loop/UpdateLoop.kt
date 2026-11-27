package it.unibo.core.loop

import it.unibo.core.world.WorldSnapshot
import kotlin.time.Duration
import kotlin.time.TimeSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

/** Gives a snapshot of the world at the moment it is called. */
fun interface EnvironmentProvider {
    /** The current snapshot of the world. */
    suspend fun provide(): WorldSnapshot
}

/** Sends the [A] decided by the aggregate program to one robot. */
fun interface Actuator<A> {
    /** Applies [actuation] to robot [id]. */
    suspend fun act(id: String, actuation: A)
}

/** Shows the world to a human (log, GUI, ...). */
fun interface Boundary {
    /** Shows [world]. */
    fun output(world: WorldSnapshot)
}

/**
 * Every [period]: takes a snapshot from [provider], shows it with [boundary], runs [tick]
 * and sends every result with [actuator]. A failing round is logged and the loop goes on.
 */
suspend fun <A> updateLoop(
    period: Duration,
    provider: EnvironmentProvider,
    tick: (WorldSnapshot) -> Map<String, A>,
    actuator: Actuator<A>,
    boundary: Boundary = Boundary { },
    timeSource: TimeSource = TimeSource.Monotonic,
) {
    while (true) {
        val start = timeSource.markNow()
        try {
            val world = provider.provide()
            boundary.output(world)
            tick(world).forEach { (id, actuation) -> actuator.act(id, actuation) }
        } catch (e: CancellationException) {
            throw e
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            println("round failed: ${e.message}")
        }
        delay((period - start.elapsedNow()).coerceAtLeast(Duration.ZERO))
    }
}
