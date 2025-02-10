package com.dergruenkohl.commands.gain

import kotlinx.serialization.Serializable

typealias IncomingCollection = List<Graph>
typealias IncomingSkills = List<SkillGraph>


@Serializable
data class OutgoingGraph(
    val p1: GraphPlayer,
    val p2: GraphPlayer
)
@Serializable
data class GraphPlayer(
    val uuid: String,
    val type: String,
    val days: Int,
    val gain: Double,
    val full: Double
)

@Serializable
data class Graph(
    val timestamp: Long,
    val cropWeight: Double,
    val crops: GraphCollections,
    val pests: Pests
)
@Serializable
data class GraphCollections(
    val cactus: Long,
    val carrot: Long,
    val cocoa: Long,
    val melon: Long,
    val mushroom: Long,
    val wart: Long,
    val potato: Long,
    val pumpkin: Long,
    val cane: Long,
    val wheat: Long,
    val seeds: Long
)
@Serializable
data class Pests(
    val mite: Int,
    val cricket: Int,
    val moth: Int,
    val worm: Int,
    val slug: Int,
    val beetle: Int,
    val locust: Int,
    val rat: Int,
    val mosquito: Int,
    val fly: Int
)

@Serializable
data class SkillGraph(
    val timestamp: Long,
    val skills: Skills
)
@Serializable
data class Skills(
    val combat: Double,
    val mining: Double,
    val foraging: Double,
    val fishing: Double,
    val enchanting: Double,
    val alchemy: Double,
    val taming: Double,
    val carpentry: Double,
    val runecrafting: Double,
    val social: Double,
    val farming: Double,
)

@Serializable
data class OvertakeBody(
    val name1: String,
    val name2: String,
    val type: String,
    val lookup: String,
    val days: Int
)
@Serializable
data class GainBody(
    val name1: String,
    val type: String,
    val lookup: String,
    val days: Int
)