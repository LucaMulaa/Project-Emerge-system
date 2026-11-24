package it.unibo.demo

import it.nicolasfarabegoli.mktt.MkttClient
import it.unibo.core.robot.Point
import it.unibo.core.robot.Stop
import it.unibo.core.robot.goTo
import it.unibo.core.world.PerceivedWorld
import it.unibo.core.world.followPoses
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

private const val BROKER_PORT = 1883
private const val ROBOT = "000000"
private const val TARGET_ROBOT = "000001"
private const val ARRIVED_DISTANCE = 0.3
private val CONTROL_PERIOD = 100.milliseconds

/** Drives robot 000000 to where robot 000001 is, then stops it. */
fun main() = runBlocking {
    val client = MkttClient {
        brokerUrl = "localhost"
        port = BROKER_PORT
    }
    client.connect()
    val world = PerceivedWorld()
    val poses = followPoses(client, world)
    val topic = "/motors/$ROBOT"
    try {
        while (true) {
            val active = world.activeRobots()
            val pose = world.poseOf(ROBOT).takeIf { ROBOT in active }
            val target = world.poseOf(TARGET_ROBOT)?.let { Point(it.x, it.y) }
            val command = if (pose != null && target != null) goTo(pose, target, ARRIVED_DISTANCE) else Stop
            client.publish(topic, command.toPayload().encodeToByteArray())
            if (command == Stop && pose != null && target != null) break
            delay(CONTROL_PERIOD)
        }
        println("$ROBOT reached $TARGET_ROBOT")
    } finally {
        withContext(NonCancellable) {
            client.publish(topic, Stop.toPayload().encodeToByteArray())
            client.unsubscribe("/pose/+")
            poses.cancelAndJoin()
            client.disconnect()
        }
    }
}
