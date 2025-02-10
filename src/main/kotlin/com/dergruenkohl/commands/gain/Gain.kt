package com.dergruenkohl.commands.gain

import kotlin.reflect.full.memberProperties

class Gain(val uuid1:String, val searchString: String, val days: Int) {

    suspend fun getPestGain(): GraphPlayer? {
        //define the getter to properly access stored data
        val pestProperyGetter = Pests::class.memberProperties.find { it.name == searchString }!!.getter

        //uuid1
        val p1 = getCropGraph(uuid1, days)?: return null
        val p1g1 = p1.first().pests
        val p1g2 = p1.last().pests

        //call the queried data using the defined getter on the retrieved objects
        val p1FirstData = pestProperyGetter.call(p1g1) as Int
        val p1LastData = pestProperyGetter.call(p1g2) as Int

        //calculate the gains
        val p1Gains = p1LastData - p1FirstData

        println("first: $p1FirstData, last: $p1LastData")

        return transform(p1Gains.toDouble(), p1LastData.toDouble())
    }

    suspend fun getCollectionGain(): GraphPlayer? {
        //define the getter to properly access stored data
        val collectionProperyGetter = GraphCollections::class.memberProperties.find { it.name == searchString }!!.getter

        //uuid1
        val p1 = getCropGraph(uuid1, days)?: return null
        val p1g1 = p1.first().crops
        val p1g2 = p1.last().crops
        //call the queried data using the defined getter on the retrieved objects
        val p1FirstData = collectionProperyGetter.call(p1g1) as Long
        val p1LastData = collectionProperyGetter.call(p1g2) as Long


        //calculate the gains
        val p1Gains = p1LastData - p1FirstData

        return transform(p1Gains.toDouble(), p1LastData.toDouble())
    }

    suspend fun getWeightGain(): GraphPlayer? {
        //uuid1
        val p1 = getCropGraph(uuid1, days)?: return null
        val p1FirstData = p1.first().cropWeight
        val p1LastData = p1.last().cropWeight


        println("first: $p1FirstData, last: $p1LastData")

        //calculate the gains
        val p1Gains = p1LastData - p1FirstData

        return transform(p1Gains, p1LastData )
    }
    suspend fun getSkillGain(): GraphPlayer? {
        //define the getter to properly access stored data
        val skillProperyGetter = Skills::class.memberProperties.find { it.name == searchString }!!.getter

        //uuid1
        val p1 = getSkillGraph(uuid1, days)?: return null
        val p1g1 = p1.first().skills
        val p1g2 = p1.last().skills

        //call the queried data using the defined getter on the retrieved objects
        val p1FirstData = skillProperyGetter.call(p1g1) as Double
        val p1LastData = skillProperyGetter.call(p1g2) as Double



        //calculate the gains
        val p1Gains = p1LastData - p1FirstData

        return transform(p1Gains, p1LastData)
    }

    private fun transform(p1Gains: Double, p1LastData: Double): GraphPlayer {

        val divisor = (24*days).toDouble()
        println(divisor)
        println("gain: $p1Gains, avgGain: ${p1Gains/divisor}")
        //transform into outgoing Objects
        val player1 = GraphPlayer(
            uuid = uuid1,
            type = searchString,
            days = days,
            gain = p1Gains/divisor,
            full = p1LastData
        )

        return player1
    }
}
