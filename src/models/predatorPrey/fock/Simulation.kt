package models.predatorPrey.fock

import deselby.fockSpace.*
import deselby.fockSpace.extensions.semicommute
import deselby.fockSpace.extensions.times
import deselby.fockSpace.extensions.toAnnihilationIndex
import deselby.fockSpace.extensions.toCreationIndex
import deselby.std.vectorSpace.SamplableDoubleVector
import experiments.phasedMonteCarlo.monteCarlo
import experiments.reverseSummation.reversePosteriorMean
import models.predatorPrey.Params
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlin.math.absoluteValue
import kotlin.math.pow

class Simulation {
//    companion object {
//        const val GRIDSIZE = 3
//    }

    //    var samples = ArrayList<MutableCreationBasis<Agent>>()
    var D0: DeselbyGround<Agent>
    val H: FockVector<Agent>
    val haIndex: Map<Agent, List<Map.Entry<Basis<Agent>, Double>>>
    val hcIndex: Map<Agent, List<Map.Entry<Basis<Agent>, Double>>>
    val params: Params


    constructor(params: Params) {
        this.params = params
        H = calcFullHamiltonian()
        haIndex = H.toAnnihilationIndex()
        hcIndex = H.toCreationIndex()
        D0 = defaultGround()
    }


    fun defaultGround(): DeselbyGround<Agent> {
        val lambdas = HashMap<Agent, Double>()
        for (pos in 0 until params.GRIDSIZESQ) {
            lambdas[Predator(pos)] = params.lambdaPred
            lambdas[Prey(pos)] = params.lambdaPrey
        }
        return DeselbyGround(lambdas)
    }

    fun setLambdas(lambda: (Agent) -> Double) {
        val lambdas = HashMap<Agent, Double>()
        for (pos in 0 until params.GRIDSIZESQ) {
            val pred = Predator(pos)
            val prey = Prey(pos)
            lambdas[pred] = lambda(pred)
            lambdas[prey] = lambda(prey)
        }
        D0 = DeselbyGround(lambdas)
    }


    fun calcFullHamiltonian(): FockVector<Agent> {
        val H = HashFockVector<Agent>()
        for (pos in 0 until params.GRIDSIZESQ) {
            Predator(pos).hamiltonian(H, params)
            Prey(pos).hamiltonian(H, params)
        }
        return H
    }


    fun monteCarloIntegrateParallel(startState: CreationBasis<Agent>, nSamples: Int, nThreads: Int, integrationTime: Double): CreationVector<Agent> {
        val reducedHamiltonian = H * startState.asGroundedBasis(D0)

        val threadTotals = Array(nThreads) {
            GlobalScope.async {
                val total = HashCreationVector<Agent>()
                val possibleTransitionStates = SamplableDoubleVector(reducedHamiltonian)
                val threadQuota = nSamples.div(nThreads) + if (it < nSamples.rem(nThreads)) 1 else 0
                for (i in 1..threadQuota) {
                    val mcSample = startState.asGroundedBasis(D0).monteCarlo(haIndex, possibleTransitionStates, integrationTime)
                    total += mcSample
                }
                total / nSamples.toDouble()
            }
        }

        val allTotal = runBlocking {
            val sum = HashCreationVector<Agent>()
            threadTotals.forEach {
                sum += it.await()
            }
            sum
        }
        return allTotal
    }


    fun monteCarloIntegrate(startState: CreationBasis<Agent>, nSamples: Int, integrationTime: Double): CreationVector<Agent> {
        val reducedHamiltonian = H * startState.asGroundedBasis(D0)
        println("Reduced hamiltonian size = ${reducedHamiltonian.size}")
        val total = HashCreationVector<Agent>()
        val possibleTransitionStates = SamplableDoubleVector(reducedHamiltonian)
        for (i in 1..nSamples) {
            val mcSample = startState.asGroundedBasis(D0).monteCarlo(haIndex, possibleTransitionStates, integrationTime)
            total += mcSample
//            if(i.rem(2000) == 0) println(mcSample)
        }
        return total / nSamples.toDouble()
    }


    fun reverseIntegrateToBasis(startState: CreationBasis<Agent>, integrationTime: Double) =
            DeselbyGround(D0.lambdas.keys.associateWith {
                reverseIntegrateToBasis(it, startState, integrationTime)
            })


    // calculates sum a_d exp(Ht) startState
    //  = sum sum_i a_d H^i/i! = sum_i sum [^i a_d, H]/i!
    // returns the mean of state d at time `integrationTime'
    fun reverseIntegrateToBasis(d: Agent, startState: CreationBasis<Agent>, integrationTime: Double): Double {
        var ithTerm = ActionBasis(emptyMap(), d).toVector()
        var mean = 0.0
        val startGround = startState.asGroundedBasis(D0)
        var increment = (ithTerm * startGround).values.sum()
        mean += increment
        var i = 0
        while (increment.absoluteValue > 0.001) {
            ++i
            val nextTerm = HashFockVector<Agent>()
            val taylorWeight = integrationTime / i
            ithTerm.semicommute(hcIndex) { basis, weight ->
                nextTerm.plusAssign(Basis.newBasis(emptyMap(), basis.annihilations), weight * taylorWeight)
//                nextTerm.plusAssign(basis, weight*taylorWeight) // without stripping
            }
            ithTerm = nextTerm
            increment = (ithTerm * startGround).values.sum()
            mean += increment
            // Truncate small terms
            nextTerm.entries.removeAll { term ->
                var basisWeight = 0.0
                startGround.preMultiply(term.key) { _, weight ->
                    basisWeight += weight.absoluteValue
                }
                (term.value * basisWeight).absoluteValue < 1e-6
            }
//            println("ithTerm = $ithTerm")
            println("iteration $i termSize = ${ithTerm.size} increment = $increment mean = $mean")
        }
        println("mean $d = $mean")
        return mean
    }

    // Calculates ae^{Ht}
    fun reverseExponential(a: Basis<Agent>, integrationTime: Double, stripCreations: Boolean = false): FockVector<Agent> {
        val exponential = HashFockVector<Agent>()
        var ithTerm = a.toVector()
        var increment = (ithTerm * D0).values.sum()
        exponential += ithTerm
        var i = 0
        while (increment.absoluteValue > 0.001) {
            ++i
            val nextTerm = HashFockVector<Agent>()
            val taylorWeight = integrationTime / i
            ithTerm.semicommute(hcIndex) { basis, weight ->
                if (stripCreations)
                    nextTerm.plusAssign(Basis.newBasis(emptyMap(), basis.annihilations), weight * taylorWeight)
                else
                    nextTerm.plusAssign(basis, weight * taylorWeight)
            }
            ithTerm = nextTerm
            // Truncate small terms
            nextTerm.entries.removeAll { term ->
                var basisWeight = 0.0
                D0.preMultiply(term.key) { _, weight ->
                    basisWeight += weight.absoluteValue
                }
                (term.value * basisWeight).absoluteValue < 1e-6
            }
            increment = (ithTerm * D0).values.sum()
            exponential += ithTerm
            println("iteration $i termSize = ${ithTerm.size} increment = $increment")
        }
        return exponential
    }


    fun reverseExponential(a: Basis<Agent>, integrationTime: Double, order: Int, stripCreations: Boolean = false): FockVector<Agent> {
        val exponential = HashFockVector<Agent>()
        var ithTerm = a.toVector()
        exponential += ithTerm
        for (i in 1..order) {
            val nextTerm = HashFockVector<Agent>()
            val taylorWeight = integrationTime / i
            ithTerm.semicommute(hcIndex) { basis, weight ->
                if (stripCreations)
                    nextTerm.plusAssign(Basis.newBasis(emptyMap(), basis.annihilations), weight * taylorWeight)
                else
                    nextTerm.plusAssign(basis, weight * taylorWeight)
            }
            ithTerm = nextTerm
            // Truncate small terms
            nextTerm.entries.removeAll { term ->
                var basisWeight = 0.0
                D0.preMultiply(term.key) { _, weight ->
                    basisWeight += weight.absoluteValue
                }
                (term.value * basisWeight).absoluteValue < 1e-8
            }
            exponential += ithTerm
            println("iteration $i termSize = ${ithTerm.size} order = $i")
        }
        return exponential
    }

    fun reverseMarginalisedIntegrate(notMarginalised: List<Agent>, integrationTime: Double): FockVector<Agent> {
        val exponential = HashFockVector<Agent>()
        val marginalisedH = marginaliseCreations(H, notMarginalised)
        exponential += Basis.identity<Agent>().toVector()
        var ithTerm = marginalisedH * integrationTime
        var increment = ithTerm.normL1()
        exponential += ithTerm
        var i = 1
        println(ithTerm)
        println("iteration $i termSize = ${ithTerm.size} increment = $increment")
        while (increment > 0.001) {
            ++i
            val nextTerm = HashFockVector<Agent>()
            val taylorWeight = integrationTime / i
            ithTerm.semicommute(hcIndex) { basis, weight ->
                val creations = HashMap<Agent, Int>()
                notMarginalised.forEach { retainedAgent ->
                    basis.creations[retainedAgent]?.also { creations[retainedAgent] = it }
                }
                nextTerm.plusAssign(Basis.newBasis(creations, basis.annihilations), weight * taylorWeight)
            }

            // Truncate small terms
//            nextTerm.entries.removeAll { term ->
//                var basisWeight = 0.0
//                D0.preMultiply(term.key) { _, weight ->
//                    basisWeight += weight.absoluteValue
//                }
//                (term.value * basisWeight).absoluteValue < 1e-6
//            }

            nextTerm.entries.removeAll { term ->
                (0.25).pow(term.key.annihilations.size) * term.value < 1e-6
            }

            val leadingHTerm = marginalisedH * ithTerm

            ithTerm = nextTerm + leadingHTerm
            increment = (ithTerm * D0).normL1()
            exponential += ithTerm
            println("iteration $i termSize = ${ithTerm.size} increment = $increment")
        }
        return exponential
    }

    fun marginaliseCreations(vec: FockVector<Agent>, notMarginalised: List<Agent>): FockVector<Agent> {
        val marginalisation = HashFockVector<Agent>()
        vec.forEach { (basis, weight) ->
            val creations = HashMap<Agent, Int>()
            notMarginalised.forEach { retainedAgent ->
                basis.creations[retainedAgent]?.also { creations[retainedAgent] = it }
            }
            marginalisation.plusAssign(Basis.newBasis(creations, basis.annihilations), weight, 1e-14)
        }
        return marginalisation
    }

    fun reversePosteriorWithTranslate(startState: CreationBasis<Agent>, observations: BinomialBasis<Agent>, time: Double, expansionOrder: Int): GroundedBasis<Agent,DeselbyGround<Agent>> {
        val means = D0.lambdas.parallelMapValues { (d, lambdad) ->
            val desiredExpectation = Basis.annihilate(d.copyAt(0)).toVector()
            val relativeStartState = startState.map {it.translate(-d.pos, params.GRIDSIZE)}
            val relativeD0 = D0.map {it.translate(-d.pos, params.GRIDSIZE)}
            desiredExpectation.reversePosteriorMean(
                    hcIndex,
                    haIndex,
                    H,
                    time,
                    relativeStartState.asGroundedBasis(relativeD0),
                    observations,
                    expansionOrder,
                    true
            )
        }

        val groundBasis = CreationBasis(observations.observations.filterValues { it != 0 })
        val newGround = DeselbyGround(means.mapValues { (d, mean) -> mean - groundBasis[d] })
        return groundBasis.asGroundedBasis(newGround)
    }


    fun reversePosterior(startState: CreationBasis<Agent>, observations: BinomialBasis<Agent>, time: Double, expansionOrder: Int): GroundedBasis<Agent,DeselbyGround<Agent>> {
//        val means = D0.lambdas.mapValues { (d, lambdad) ->
        val means = D0.lambdas.parallelMapValues { (d, lambdad) ->
            val desiredExpectation = Basis.annihilate(d).toVector()
            desiredExpectation.reversePosteriorMean(hcIndex, haIndex, H, time, startState.asGroundedBasis(D0), observations, expansionOrder)
        }

        val groundBasis = CreationBasis(observations.observations.filterValues { it != 0 })
        val newGround = DeselbyGround(means.mapValues { (d, mean) -> mean - groundBasis[d] })
        return groundBasis.asGroundedBasis(newGround)
    }


    fun<K,V,R> Map<K,V>.parallelMapValues(f: (Map.Entry<K,V>) -> R): Map<K,R> {
        val futures = this.map {
            val future = GlobalScope.async { f(it) }
            Pair(it.key, future)
        }
        val results = HashMap<K,R>(futures.size)
        runBlocking {
            futures.forEach {(key, future) ->
                results[key] = future.await()
            }
        }
        return results
    }
}