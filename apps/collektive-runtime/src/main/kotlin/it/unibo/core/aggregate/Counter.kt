package it.unibo.core.aggregate

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.stdlib.accumulation.countDevices
import it.unibo.collektive.stdlib.spreading.hopGradientCast

/**
 * Counts the robots in the network and makes every robot know the total.
 * The count is accumulated in the [sink] and then broadcast to everyone.
 * */
fun Aggregate<String>.countRobots(sink: Boolean): Int {
    val numberRobot = countDevices(sink = sink)
    return hopGradientCast(
        source = sink,
        local = numberRobot,
    )
}
