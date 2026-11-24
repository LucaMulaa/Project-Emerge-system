package it.unibo.core.aggregate

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.stdlib.consensus.boundedElection

/** How many hops a leader can reach: 12 robots in a line are 11 hops. */
private const val LEADER_BOUND = 11

/**
 * Elects one leader in every island of connected robots: the robot with the highest ID.
 */
fun Aggregate<String>.electLeader(): String = boundedElection(LEADER_BOUND)

/**
 * Counts the robots of every island, using the leader of the island as the sink.
 */
fun Aggregate<String>.countRobotsPerIsland(): Int = countRobots(sink = localId == electLeader())
