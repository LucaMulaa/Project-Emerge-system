package it.unibo.demo

import it.nicolasfarabegoli.mktt.MkttClient
import it.unibo.core.aggregate.AggregateOrchestrator
import it.unibo.core.flock.Vector
import it.unibo.core.flock.flock
import it.unibo.core.loop.updateLoop
import it.unibo.core.robot.MotorCommand
import it.unibo.core.robot.Point
import it.unibo.core.robot.goTo
import it.unibo.core.world.RobotPose
import it.unibo.core.world.WorldSnapshot
import it.unibo.mqtt.MqttEnvironmentProvider
import it.unibo.mqtt.MqttMotorsActuator
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

private const val BROKER_PORT = 1883
private const val STOP_SPEED = 0.15
private val WAYPOINT = Point(-20.0, -20.0)
private val ROUND_TIME = 100.milliseconds
private val RUN_TIME = 60.seconds

/** Moves all the robots of the simulator as a flock towards [WAYPOINT] for [RUN_TIME], then stops them. */
fun main(): Unit = runBlocking {
    val client = MkttClient {
        brokerUrl = "localhost"
        port = BROKER_PORT
    }
    client.connect()
    val provider = MqttEnvironmentProvider(client)
    val subscriptions = provider.start(this)
    val motors = MqttMotorsActuator(client)
    var current = WorldSnapshot(emptyMap(), emptyMap())
    val orchestrator = AggregateOrchestrator(
        neighborsOf = { id -> current.neighborsOf(id) },
        program = { flock(current.poses.getValue(localId), WAYPOINT) },
    )
    try {
        withTimeoutOrNull(RUN_TIME) {
            updateLoop(
                period = ROUND_TIME,
                provider = provider,
                tick = { world ->
                    current = world
                    orchestrator.tick(world.robots).mapValues { (id, velocity) ->
                        toCommand(world.poses.getValue(id), velocity)
                    }
                },
                actuator = motors,
            )
        }
    } finally {
        withContext(NonCancellable) {
            motors.stopAll()
            client.unsubscribe("/neighbors/+")
            client.unsubscribe("/pose/+")
            subscriptions.forEach { it.cancelAndJoin() }
            client.disconnect()
        }
    }
}

/** Turns the [velocity] of the flock into a motor command: go to where the velocity points. */
private fun toCommand(pose: RobotPose, velocity: Vector): MotorCommand =
    goTo(pose, Point(pose.x + velocity.x, pose.y + velocity.y), STOP_SPEED)
