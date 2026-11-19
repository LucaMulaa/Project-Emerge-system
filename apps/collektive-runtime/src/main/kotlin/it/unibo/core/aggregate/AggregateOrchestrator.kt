package it.unibo.core.aggregate

import it.unibo.collektive.Collektive
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.DataSharingMethod
import it.unibo.collektive.networking.Mailbox
import it.unibo.collektive.networking.Message
import it.unibo.collektive.networking.NeighborsData
import it.unibo.collektive.networking.OutboundEnvelope
import it.unibo.collektive.path.Path

/**
 * Runs an aggregate [program] for many robots inside one process.
 */
class AggregateOrchestrator<R>(
    private val neighborsOf: (String) -> Set<String>,
    private val program: Aggregate<String>.() -> R,
) {
    private val robots = mutableMapOf<String, Robot>()

    /** Runs one round for every robot in [ids], and returns the result of each robot. */
    fun tick(ids: Set<String>): Map<String, R> {
        robots.keys.retainAll(ids)
        ids.forEach { id -> robots.getOrPut(id) { Robot(id) } }
        return robots.mapValues { (_, robot) -> robot.collektive.cycle() }
    }

    private inner class Robot(val id: String) {
        val inbox = mutableMapOf<String, Message<String, *>>()
        val collektive = Collektive(id, InMemoryMailbox(this)) { program() }
    }

    private inner class InMemoryMailbox(private val me: Robot) : Mailbox<String> {
        override val inMemory: Boolean = true

        private fun liveNeighbors(): Set<String> = neighborsOf(me.id)
            .filter { it != me.id && it in robots }
            .toSet()

        override fun deliverableFor(outboundMessage: OutboundEnvelope<String>) {
            liveNeighbors().forEach { neighborId ->
                robots.getValue(neighborId).inbox[me.id] = outboundMessage.prepareMessageFor(neighborId)
            }
        }

        override fun deliverableReceived(message: Message<String, *>): Unit =
            error("In-memory network: messages are written directly into the neighbour's inbox")

        override fun currentInbound(): NeighborsData<String> {
            me.inbox.keys.retainAll(liveNeighbors())
            val snapshot = me.inbox.toMap()
            return object : NeighborsData<String> {
                override val neighbors: Set<String> get() = snapshot.keys

                @Suppress("UNCHECKED_CAST")
                override fun <Value> dataAt(
                    path: Path,
                    dataSharingMethod: DataSharingMethod<Value>,
                ): Map<String, Value> = snapshot
                    .filterValues { path in it.sharedData }
                    .mapValues { (_, message) -> message.sharedData[path] as Value }
            }
        }
    }
}
