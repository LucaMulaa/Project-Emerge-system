package it.unibo.core.robot

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import it.unibo.core.world.RobotPose
import kotlin.math.PI

// The simulator says 0 when the robot faces +y: a heading of -PI/2 means "facing +x".
private val facingX = RobotPose(x = 0.0, y = 0.0, heading = -PI / 2)

class GoToPointTest : StringSpec({
    "a robot on the target stops" {
        goTo(facingX, Point(0.0, 0.05)) shouldBe Stop
    }

    "a target straight ahead makes the robot drive straight" {
        val move = goTo(facingX, Point(2.0, 0.0)).shouldBeInstanceOf<Move>()
        move.left shouldBe (move.right plusOrMinus 1e-9)
        (move.left > 0) shouldBe true
    }

    "a target on the left makes the robot spin to the left" {
        val move = goTo(facingX, Point(0.0, 2.0)).shouldBeInstanceOf<Move>()
        (move.left < 0) shouldBe true
        (move.right > 0) shouldBe true
    }

    "a target on the right makes the robot spin to the right" {
        val move = goTo(facingX, Point(0.0, -2.0)).shouldBeInstanceOf<Move>()
        (move.right < 0) shouldBe true
        (move.left > 0) shouldBe true
    }

    "angles are taken the short way" {
        val normAngle = normalizeAngle(2 * PI - 0.1)
        normAngle shouldBe (-0.1 plusOrMinus 1e-9)
    }

    "the direction is measured from the robot, not from the origin" {
        val farAway = facingX.copy(x = 10.0, y = 10.0)
        val move = goTo(farAway, Point(12.0, 10.0)).shouldBeInstanceOf<Move>()
        move.left shouldBe (move.right plusOrMinus 1e-9)
        (move.left > 0) shouldBe true
    }

    "a target slightly to the left makes the robot curve while driving" {
        val move = goTo(facingX, Point(2.0, 0.5)).shouldBeInstanceOf<Move>()
        (move.left > 0) shouldBe true
        (move.right > 0) shouldBe true
        (move.left < move.right) shouldBe true
    }
})
