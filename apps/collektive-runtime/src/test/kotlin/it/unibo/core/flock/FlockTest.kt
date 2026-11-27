package it.unibo.core.flock

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.shouldBe
import it.unibo.core.aggregate.AggregateOrchestrator
import it.unibo.core.flock.Vector.Companion.ZERO
import it.unibo.core.robot.Point
import it.unibo.core.world.RobotPose
import kotlin.math.PI

class FlockTest : StringSpec({

    "Only one robot to waypoint" {
        val robot = RobotPose(0.0, 0.0, 0.0)
        val waypoint = Point(10.0, 0.0)
        val velocity = flockVelocity(robot, emptyList(), waypoint)
        velocity.x shouldBeGreaterThan 0.0
        velocity.y shouldBe (0.0 plusOrMinus 1e-9)
    }

    "Only one robot stand on the waypoint" {
        val robot = RobotPose(1.0, 1.0, 0.0)
        val waypoint = Point(1.0, 1.0)
        flockVelocity(robot, emptyList(), waypoint) shouldBe ZERO
    }

    "Only one robot go to a group of robots" {
        val robot = RobotPose(0.0, 0.5, 0.0)
        val neighbors = listOf(RobotPose(5.0, 0.0, 0.0), RobotPose(5.0, 5.1, 0.0))
        val waypoint = Point(0.0, 0.5)
        flockVelocity(robot, neighbors, waypoint).x shouldBeGreaterThan 0.0
    }

    "Too close robots move apart" {
        val robot = RobotPose(0.0, 0.0, -PI / 2)
        val neighbors = listOf(RobotPose(0.1, 0.0, -PI / 2))
        val waypoint = Point(0.0, 0.0)
        flockVelocity(robot, neighbors, waypoint).x shouldBeLessThan 0.0
    }

    "Every robot sees the poses of its neighbours" {
        val poses = mapOf("000000" to RobotPose(0.0, 0.0, 0.0), "000001" to RobotPose(4.0, 0.0, 0.0))
        val orchestrator = AggregateOrchestrator(
            neighborsOf = { id -> poses.keys - id },
            program = { flock(poses.getValue(localId), waypoint = Point(0.0, 0.0)) },
        )
        val velocities = (1..3).map { orchestrator.tick(poses.keys) }.last()
        velocities.getValue("000000").x shouldBeGreaterThan 0.0
    }
})
