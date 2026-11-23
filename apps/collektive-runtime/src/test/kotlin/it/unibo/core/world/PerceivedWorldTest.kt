package it.unibo.core.world

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TestTimeSource
import kotlinx.serialization.json.Json

class PerceivedWorldTest : StringSpec({
    "a pose is read from the JSON of the simulator" {
        val payload = """
            {"x_m": 1.5, "y_m": -0.25, "heading_rad": 3.14, "speed_m_s": 0.0,
             "position_variance_m2": 0.01, "timestamp_us": 1700000000000000}
        """.trimIndent()
        val json = Json { ignoreUnknownKeys = true }
        json.decodeFromString<RobotPose>(payload) shouldBe RobotPose(x = 1.5, y = -0.25, heading = 3.14)
    }

    "a robot whose pose is too old leaves the world" {
        val time = TestTimeSource()
        val world = PerceivedWorld(timeSource = time)
        world.updatePose("000001", RobotPose(1.0, 2.0, 0.0))
        world.activeRobots() shouldBe setOf("000001")
        time += 400.milliseconds
        world.activeRobots() shouldBe emptySet()
    }

    "a recent pose keeps the robot in the world" {
        val time = TestTimeSource()
        val world = PerceivedWorld(timeSource = time)
        world.updatePose("000001", RobotPose(1.0, 2.0, 0.0))
        time += 200.milliseconds
        world.updatePose("000001", RobotPose(1.1, 2.0, 0.0))
        time += 200.milliseconds
        world.activeRobots() shouldBe setOf("000001")
        world.poseOf("000001") shouldBe RobotPose(1.1, 2.0, 0.0)
    }

    "the neighbours are the last ones published, or none" {
        val world = PerceivedWorld()
        world.updateNeighbors("000001", setOf("000001", "000002"))
        world.updateNeighbors("000001", setOf("000001", "000003"))
        world.neighborsOf("000001") shouldBe setOf("000001", "000003")
        world.neighborsOf("000009") shouldBe emptySet()
    }
})
