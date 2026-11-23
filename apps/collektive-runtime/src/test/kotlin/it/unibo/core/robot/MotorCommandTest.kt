package it.unibo.core.robot

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class MotorCommandTest : StringSpec({
    "a move becomes the JSON of the protocol" {
        Move(0.3, -0.3).toPayload() shouldBe """{"Move":{"left":0.3,"right":-0.3}}"""
    }

    "a move out of range is clamped to -1 and 1" {
        Move(5.0, -7.0).toPayload() shouldBe """{"Move":{"left":1.0,"right":-1.0}}"""
    }

    "stop becomes the JSON string Stop" {
        Stop.toPayload() shouldBe "\"Stop\""
    }
})
