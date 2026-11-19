package it.unibo.core.aggregate

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class CounterTest : StringSpec({
    val ids = (0 until 12).map { it.toString(16).uppercase().padStart(6, '0') }.toSet()

    "every robot counts 12 when all robots are connected" {
        val orchestrator = AggregateOrchestrator(
            neighborsOf = { ids },
            program = { countRobots(sink = localId == "000000") },
        )
        val results = (1..10).map { orchestrator.tick(ids) }.last()
        results shouldBe ids.associateWith { 12 }
    }
    val idsList = (0 until 12).map { it.toString(16).uppercase().padStart(6, '0') }
    "every robot counts 12 when all robots are in a line" {
        val orchestrator = AggregateOrchestrator(
            neighborsOf = { id ->
                val i = idsList.indexOf(id)
                setOfNotNull(idsList.getOrNull(i - 1), idsList.getOrNull(i + 1))
            },
            program = { countRobots(sink = localId == "00000B") },
        )
        val results = (1..36).map { orchestrator.tick(ids) }.last()
        results shouldBe ids.associateWith { 12 }
    }
})
