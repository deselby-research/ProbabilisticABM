package experiments.reverseSummation

import deselby.fockSpace.Basis
import deselby.fockSpace.FockVector
import deselby.fockSpace.HashFockVector
import deselby.fockSpace.extensions.semicommute
import deselby.fockSpace.extensions.stripCreations
import deselby.fockSpace.extensions.times
import deselby.std.combinatorics.choose
import models.predatorPrey.TenByTenParams
import models.predatorPrey.fock.Agent
import models.predatorPrey.fock.Predator
import models.predatorPrey.fock.Simulation
import org.junit.Test
import kotlin.math.pow

class OrderReduction {
    @Test
    fun reverseIntegrationWithOrderReduction() {
        val sim = Simulation(TenByTenParams)
        val a = Basis.annihilate<Agent>(Predator(0)).toVector()
//        val a = Basis.annihilate<Agent>(Prey(1))
//        val a = Basis.newBasis(emptyMap(),mapOf(Prey(1) as Agent to 1, Predator(0) as Agent to 1))

        val n = 5
        var aHi = a
        for(i in 1..n) {
            aHi = aHi.semicommute(sim.hcIndex).stripCreations()
            println("aH$i size = ${aHi.size} sum = ${(aHi * sim.D0).values.sum()}")
//            println(aHi)
//            println(aHi * sim.D0)
        }
        println()
        aHi = a
        for(i in 1..n) {
            println("unreduced size = ${aHi.size}")
            println("unreduced sum = ${(aHi.semicommute(sim.hcIndex).stripCreations() * sim.D0).values.sum()}")
            aHi = reduceAnnihilation2ndOrder(aHi, sim.D0.lambdas)
            println("reduced size = ${aHi.size}")
            aHi = aHi.semicommute(sim.hcIndex).stripCreations()
            println("aH${i}r size = ${aHi.size} sum = ${(aHi * sim.D0).values.sum()}")
        }
        println()

//        val aH2 = a.semicommute(sim.hcIndex).stripCreations().semicommute(sim.hcIndex).stripCreations()
//        println("aH2 size = ${aH2.size}")
//        val aH3 = aH2.semicommute(sim.hcIndex).stripCreations()
//        println("aH3 size = ${aH3.size}")
//        println("H3 sum = ${(aH3 * sim.D0).values.sum()}")
//        val aH2reduced = reduceAnnihilation2ndOrder(aH2, sim.D0.lambdas)
//        println("aH2reduced size = ${aH2reduced.size}")
//        val aH3reduced = aH2reduced.semicommute(sim.hcIndex).stripCreations()
//        println("aH3reduced size = ${aH3reduced.size}")
//        println("H3reduced sum = ${(aH3reduced * sim.D0).values.sum()}")


    }


    fun reduceAnnihilation2ndOrder(vec: FockVector<Agent>, lambdas: Map<Agent,Double>): FockVector<Agent> {
        val reducedVec = HashFockVector<Agent>()
        vec.entries.forEach { (basis, weight) ->
            var prodOfLambdas = 1.0
            basis.annihilations.forEach {
                prodOfLambdas *= lambdas[it.key]?.pow(it.value)?:0.0
            }
            if(basis.annihilations.values.sum() > 2) {
                secondOrderBases(basis.annihilations).forEach { reducedAnnihilations ->
                    val a1 = reducedAnnihilations.keys.first()
                    val a2 = reducedAnnihilations.keys.last()
                    reducedVec.plusAssign(Basis.newBasis(basis.creations, reducedAnnihilations), weight * prodOfLambdas / ((lambdas[a1]
                            ?: 0.0) * (lambdas[a2] ?: 0.0)))
                }
                basis.annihilations.forEach { (agent, occupation) ->
//                    if(occupation > 1) println(">1 occupation")
                    for (i in 1..occupation) {
                        reducedVec.plusAssign(Basis.newBasis(basis.creations, mapOf(agent to 1)), -weight * prodOfLambdas / (lambdas[agent]
                                ?: 0.0))
                    }
                }
            } else {
                reducedVec.plusAssign(basis, weight)
            }
        }
        return reducedVec
    }

    fun secondOrderBases(agents: Map<Agent,Int>): List<Map<Agent,Int>> {
        val order = agents.values.sum()
        if(order < 3) return listOf(agents)
        val agentList = ArrayList<Agent>(order)
        agents.forEach {
            for(i in 1..it.value) {
                agentList.add(it.key)
            }
        }
        val mapList = ArrayList<Map<Agent,Int>>(order.choose(2).toInt())
        for(i in 0 until order-1) {
            for(j in i+1 until order) {
                if(agentList[i] != agentList[j])
                    mapList.add(mapOf(agentList[i] to 1, agentList[j] to 1))
                else
                    mapList.add(mapOf(agentList[i] to 2))
            }
        }
        return mapList
    }
}