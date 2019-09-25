package experiments.reverseSummation

import deselby.fockSpace.*
import deselby.fockSpace.extensions.*
import experiments.spatialPredatorPrey.TenByTenParams
import experiments.spatialPredatorPrey.fock.Agent
import experiments.spatialPredatorPrey.fock.Predator
import experiments.spatialPredatorPrey.fock.Simulation
import org.apache.commons.math3.distribution.PoissonDistribution
import org.junit.Test

class ReverseBinomial {

    @Test
    fun reverseIntegrationWithLGOperator() {
        val sim = Simulation(TenByTenParams)
        val d : Agent = Predator(0)
        val a = Basis.annihilate(d).toVector()
        val n = 4
        var aHi = a
        // [AG,H] = [A,H]G + A[G,H]
        val LgcommuteH = LgOperator(d, 0.5).commute(sim.H)
        println(LgcommuteH.size)
        println(LgcommuteH)
        for(i in 1..n) {

            aHi = aHi.semiCommuteAndStrip(sim.hcIndex) + aHi.multiplyAndStrip(LgcommuteH) + aHi

            println()
            val sum = (aHi * sim.D0).values.sum()
            println("iteration $i")
            println("aH$i size = ${aHi.size} sum = $sum  taylorWeight (t=0.5) = ${sum*PoissonDistribution(0.5).probability(i)}")
        }
    }


    @Test
    fun reverseIntegrationWithGOperator() {
        val sim = Simulation(TenByTenParams)
        val d : Agent = Predator(0)
        val a = Basis.annihilate(d).toVector()
        val n = 4
        var aHi = a
        // [AG,H] = [A,H]G + A[G,H]
        val GcommuteH = GOperator(d, 0.5, sim.D0.lambda(d)).commute(sim.H)
        println(GcommuteH.size)
        println(GcommuteH)
        for(i in 1..n) {

//            aHi = (aHi.semicommute(sim.hcIndex).stripCreations() + (aHi*GcommuteH).stripCreations()) //.filterBelow(1e-5*iFactorial*(2.0.pow(i)))
//            println((aHi.semicommute(sim.hcIndex).stripCreations() + (aHi*GcommuteH).stripCreations()).size)
//            aHi = (aHi.semiCommuteAndStrip(sim.hcIndex) + (aHi*GcommuteH).stripCreations())
            aHi = aHi.semiCommuteAndStrip(sim.hcIndex) + aHi.multiplyAndStrip(GcommuteH) + aHi
//            aHi = aHi.semicommute(sim.hcIndex).stripCreations()
//            aHi = (aHi*GcommuteH).stripCreations()

            val averageOrder = aHi.keys.sumBy { it.annihilations.values.sum() }.toDouble()/aHi.size
//            for(ord in 1..9) {
//                val n = aHi.keys.count { it.annihilations.values.sum() == ord }
//                val weight = aHi.entries.filter {it.key.annihilations.values.sum() == ord }.sumByDouble { it.value.absoluteValue }
//                println("order $ord $n ${weight/n}")
//            }
            println()
            val sum = (aHi * sim.D0).values.sum()
            println("iteration $i")
            println("average order = $averageOrder")
            println("aH$i size = ${aHi.size} sum = $sum  taylorWeight (t=0.5) = ${sum*PoissonDistribution(0.5).probability(i)}")
//            println(aHi)
//            println(aHi * sim.D0)
        }
    }

    @Test
    fun footprintTest() {
        val sim = Simulation(TenByTenParams)
        val d : Agent = Predator(0)
        val a = Basis.annihilate(d)
        val order = 4

        var forwardSweep = Basis.identityCreationVector<Agent>()
        for(i in 0 until order) {
            val footprint = reverseFootprint(a, sim.hcIndex, order - i)
            val nextFootprint = reverseFootprint(a, sim.hcIndex, order - i - 1)
            val lambdas = sim.D0.lambdas.filter { (d, _) -> footprint.contains(d) }
            val D0 = DeselbyGround(lambdas)
            val H = marginaliseHamiltonian(sim.H, footprint, nextFootprint)
            val (pureH, impureH) = splitHamiltonian(sim.H, footprint, nextFootprint)
            forwardSweep = marginaliseTo(forwardSweep, footprint)
//            forwardSweep = forwardSweep.filterBelow(1e-5)
            val remarginalisedForwardSweep = marginaliseTo(forwardSweep, nextFootprint)
//            println("H = $H")
            println("footprintSize = ${footprint.size} forwardSweepSize = ${forwardSweep.size} D0size = ${D0.lambdas.size} Hsize = ${H.size}")
            println("splitH sizes: ${pureH.size} + ${impureH.size} = ${pureH.size + impureH.size}")
//            forwardSweep = remarginalisedForwardSweep * (H * D0) + H.semicommute(forwardSweep.toCreationIndex()) * D0
            val (purePart, _) = pureH * remarginalisedForwardSweep.asGroundedVector(D0)
            val (impurePart, _) = impureH.timesAndMarginalise(forwardSweep.asGroundedVector(D0), nextFootprint)
//            forwardSweep = pureH * remarginalisedForwardSweep * D0 + impureH * forwardSweep * D0
            forwardSweep = purePart + impurePart
            println(forwardSweep.size)
//            println(forwardSweep)
        }
        forwardSweep = marginaliseTo(forwardSweep, setOf(d))
        println(forwardSweep.size)
    }


    fun<AGENT> reverseFootprint(endState: Basis<AGENT>, hIndex: CreationIndex<AGENT>, order: Int): Set<AGENT> {
        var activeAgents = endState.annihilations.keys
        for(i in 1..order) {
            val nextOrderActiveAgents = HashSet<AGENT>()
            activeAgents.forEach { currentActiveAgent ->
                hIndex[currentActiveAgent]?.forEach { (hamiltonianTerm, _) ->
                    hamiltonianTerm.annihilations.forEach { (termAnnihilationAgent, _) ->
                        nextOrderActiveAgents.add(termAnnihilationAgent)
                    }
                }
            }
            activeAgents = nextOrderActiveAgents
        }
        return activeAgents
    }

    fun<AGENT> marginaliseTo(vec: CreationVector<AGENT>, activeAgents: Set<AGENT>): CreationVector<AGENT> {
        val marginalisation = HashCreationVector<AGENT>()
        vec.forEach {(basis, weight) ->
            marginalisation.plusAssign(
                    CreationBasis(basis.creations.filter {(d,_) -> activeAgents.contains(d)}),
                    weight,
                    1e-14
            )
        }
        return marginalisation
    }


    fun<AGENT> marginaliseHamiltonian(vec: FockVector<AGENT>, annihilationAgents: Set<AGENT>, creationAgents: Set<AGENT>): FockVector<AGENT> {
        val marginalisation = HashFockVector<AGENT>()
        vec.forEach {(basis, weight) ->
            if(annihilationAgents.containsAll(basis.annihilations.keys)) {
                marginalisation.plusAssign(
                        Basis.newBasis(
                                basis.creations.filter {(d,_) -> creationAgents.contains(d)},
                                basis.annihilations
                        ),
                        weight,
                        1e-14
                )
            }
        }
        return marginalisation
    }


    fun<AGENT> splitHamiltonian(H: FockVector<AGENT>, activeAgents: Set<AGENT>, nextActiveAgents: Set<AGENT>): Pair<FockVector<AGENT>,FockVector<AGENT>> {
        val purePart = HashFockVector<AGENT>()
        val impurePart = HashFockVector<AGENT>()
        H.forEach { (basis, weight) ->
            val reducedBasis = Basis.newBasis(
                    basis.creations.filter {(d,_) -> nextActiveAgents.contains(d)},
                    basis.annihilations
            )
            if(nextActiveAgents.containsAll(basis.annihilations.keys)) {
                purePart.plusAssign(reducedBasis, weight)
            } else if (activeAgents.containsAll(basis.annihilations.keys)) {
                impurePart.plusAssign(reducedBasis, weight)
            }
        }
        return Pair(purePart, impurePart)
    }



}