package it.unibo.core.loop

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import it.unibo.core.world.RobotPose
import it.unibo.core.world.WorldSnapshot
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class UpdateLoopTest : StringSpec({
    val world = WorldSnapshot(
        poses = mapOf("000001" to RobotPose(0.0, 0.0, 0.0), "000002" to RobotPose(1.0, 0.0, 0.0)),
        neighbors = emptyMap(),
    )

    "every round sends the result of the tick to every robot" {
        runTest {
            val sent = mutableListOf<Pair<String, Int>>()
            backgroundScope.launch {
                updateLoop(
                    period = 100.milliseconds,
                    provider = { world },
                    tick = { snapshot -> snapshot.robots.associateWith { 7 } },
                    actuator = { id, value -> sent.add(id to value) },
                    timeSource = testScheduler.timeSource,
                )
            }
            advanceTimeBy(250.milliseconds)
            runCurrent()
            sent.size shouldBe 6
            sent.toSet() shouldBe setOf("000001" to 7, "000002" to 7)
        }
    }

    "a failing round does not stop the loop" {
        runTest {
            var calls = 0
            val sent = mutableListOf<String>()
            backgroundScope.launch {
                updateLoop(
                    period = 100.milliseconds,
                    provider = { if (++calls == 1) error("broken JSON") else world },
                    tick = { snapshot -> snapshot.robots.associateWith { 1 } },
                    actuator = { id, _ -> sent += id },
                    timeSource = testScheduler.timeSource,
                )
            }
            advanceTimeBy(150.milliseconds)
            runCurrent()
            sent shouldBe listOf("000001", "000002")
        }
    }
})
