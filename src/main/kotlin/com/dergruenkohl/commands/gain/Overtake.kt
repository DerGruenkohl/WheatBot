package com.dergruenkohl.commands.gain

import kotlin.reflect.full.memberProperties


class Overtake(private val uuid1:String, private val uuid2:String, private val searchString: String, private val days: Int) {

    suspend fun getPestOvertake(): OutgoingGraph? {
        //define the getter to properly access stored data
        val pestProperyGetter = Pests::class.memberProperties.find { it.name == searchString }!!.getter

        //uuid1
        val p1 = getCropGraph(uuid1, days)?: return null
        val p1g1 = p1.first().pests
        val p1g2 = p1.last().pests

        //uuid2
        val p2 = getCropGraph(uuid2, days)?: return null
        val p2g1 = p2.first().pests
        val p2g2 = p2.last().pests

        //call the queried data using the defined getter on the retrieved objects
        val p1FirstData = pestProperyGetter.call(p1g1) as Int
        val p1LastData = pestProperyGetter.call(p1g2) as Int

        val p2FirstData = pestProperyGetter.call(p2g1) as Int
        val p2LastData = pestProperyGetter.call(p2g2) as Int

        //calculate the gains
        val p1Gains = p1LastData - p1FirstData
        val p2Gains = p2LastData - p2FirstData

        println("first: $p1FirstData, last: $p1LastData \n first: $p2FirstData, last: $p2LastData")

        return transform(p1Gains.toDouble(), p2Gains.toDouble(), p1LastData.toDouble(), p2LastData.toDouble())
    }

    suspend fun getCollectionOvertake(): OutgoingGraph? {
        //define the getter to properly access stored data
        val collectionProperyGetter = GraphCollections::class.memberProperties.find { it.name == searchString }!!.getter

        //uuid1
        val p1 = getCropGraph(uuid1, days)?: return null
        val p1g1 = p1.first().crops
        val p1g2 = p1.last().crops

        //uuid2
        val p2 = getCropGraph(uuid2, days)?: return null
        val p2g1 = p2.first().crops
        val p2g2 = p2.last().crops

        //call the queried data using the defined getter on the retrieved objects
        val p1FirstData = collectionProperyGetter.call(p1g1) as Long
        val p1LastData = collectionProperyGetter.call(p1g2) as Long

        val p2FirstData = collectionProperyGetter.call(p2g1) as Long
        val p2LastData = collectionProperyGetter.call(p2g2) as Long


        //calculate the gains
        val p1Gains = p1LastData - p1FirstData
        val p2Gains = p2LastData - p2FirstData

        return transform(p1Gains.toDouble(), p2Gains.toDouble(), p1LastData.toDouble(), p2LastData.toDouble())
    }

    suspend fun getWeightOvertake(): OutgoingGraph? {
        //uuid1
        val p1 = getCropGraph(uuid1, days)?: return null
        val p1FirstData = p1.first().cropWeight
        val p1LastData = p1.last().cropWeight

        //uuid2
        val p2 = getCropGraph(uuid2, days)?: return null
        val p2FirstData = p2.first().cropWeight
        val p2LastData = p2.last().cropWeight

        println("first: $p1FirstData, last: $p1LastData \n first: $p2FirstData, last: $p2LastData")

        //calculate the gains
        val p1Gains = p1LastData - p1FirstData
        val p2Gains = p2LastData - p2FirstData

        return transform(p1Gains, p2Gains, p1LastData, p2LastData)
    }
    suspend fun getSkillOverTake(): OutgoingGraph? {
        //define the getter to properly access stored data
        val skillProperyGetter = Skills::class.memberProperties.find { it.name == searchString }!!.getter

        //uuid1
        val p1 = getSkillGraph(uuid1, days)?: return null
        val p1g1 = p1.first().skills
        val p1g2 = p1.last().skills

        //uuid2
        val p2 = getSkillGraph(uuid2, days)?: return null
        val p2g1 = p2.first().skills
        val p2g2 = p2.last().skills

        //call the queried data using the defined getter on the retrieved objects
        val p1FirstData = skillProperyGetter.call(p1g1) as Double
        val p1LastData = skillProperyGetter.call(p1g2) as Double

        val p2FirstData = skillProperyGetter.call(p2g1) as Double
        val p2LastData = skillProperyGetter.call(p2g2) as Double


        //calculate the gains
        val p1Gains = p1LastData - p1FirstData
        val p2Gains = p2LastData - p2FirstData

        return transform(p1Gains, p2Gains, p1LastData, p2LastData)
    }

    private fun transform(p1Gains: Double, p2Gains: Double, p1LastData: Double, p2LastData: Double): OutgoingGraph {

        val divisor = (24*days).toDouble()
        println(divisor)
        println("gain: $p1Gains, avgGain: ${p1Gains/divisor}")
        println("gain: $p2Gains, avgGain: ${p2Gains/divisor}")

        //transform into outgoing Objects
        val player1 = GraphPlayer(
            uuid = uuid1,
            type = searchString,
            days = days,
            gain = p1Gains/divisor,
            full = p1LastData
        )
        val player2 = GraphPlayer(
            uuid = uuid2,
            type = searchString,
            days = days,
            gain = p2Gains/divisor,
            full = p2LastData
        )
        val gain = OutgoingGraph(
            player1,
            player2,
        )
        return gain
    }
}
