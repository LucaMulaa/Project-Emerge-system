package it.unibo.core.aggregate

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

private val ids = (0 until 12).map { it.toString(16).uppercase().padStart(6, '0') }
private val leftIsland = ids.take(7).toSet()
private val rightIsland = ids.drop(7).toSet()

// Two islands that cannot see each other: 000000..000006 and 000007..00000B.
private fun islandOf(id: String): Set<String> = if (id in leftIsland) leftIsland else rightIsland

class LeaderTest : StringSpec({
    "every island elects the robot with the highest ID" {
        val orchestrator = AggregateOrchestrator(neighborsOf = ::islandOf, program = { electLeader() })
        val results = (1..10).map { orchestrator.tick(ids.toSet()) }.last()
        val expectedLeft = leftIsland.associateWith { "000006" }
        val expectedRight = rightIsland.associateWith { "00000B" }
        results shouldBe expectedLeft + expectedRight
    }

    "every island counts its own robots" {
        val orchestrator = AggregateOrchestrator(neighborsOf = ::islandOf, program = { countRobotsPerIsland() })
        val results = (1..10).map { orchestrator.tick(ids.toSet()) }.last()
        val expectedLeft = leftIsland.associateWith { 7 }
        val expectedRight = rightIsland.associateWith { 5 }
        results shouldBe expectedLeft + expectedRight
    }

    "the count does not grow forever when the leader leaves" {
        val orchestrator = AggregateOrchestrator(neighborsOf = ::islandOf, program = { countRobotsPerIsland() })
        repeat(10) { orchestrator.tick(ids.toSet()) }
        val withoutLeader = ids.toSet() - "00000B"

        val results = (1..40).map { orchestrator.tick(withoutLeader) }.last()
        val expectedLeft = leftIsland.associateWith { 7 }
        val expectedRight = (rightIsland - "00000B").associateWith { 4 }
        results shouldBe expectedLeft + expectedRight
    }
})
