package experiments.spatialPredatorPrey.fock

import deselby.fockSpace.*
import deselby.fockSpace.extensions.times
import deselby.fockSpace.extensions.toAnnihilationIndex
import deselby.std.vectorSpace.SamplableDoubleVector
import experiments.phasedMonteCarlo.monteCarlo
import experiments.spatialPredatorPrey.Params
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

class Simulation {
//    companion object {
//        const val GRIDSIZE = 3
//    }

//    var samples = ArrayList<MutableCreationBasis<Agent>>()
    var D0: DeselbyGround<Agent>
    val H: FockVector<Agent>
    val hIndex: Map<Agent, List<Map.Entry<Basis<Agent>, Double>>>
    val params: Params


    constructor(params: Params) {
        this.params = params
        H = calcFullHamiltonian()
        hIndex = H.toAnnihilationIndex()
        val lambdas = HashMap<Agent,Double>()
        for(pos in 0 until params.GRIDSIZESQ) {
            lambdas[Predator(pos)] = params.lambdaPred
            lambdas[Prey(pos)] = params.lambdaPrey
        }
        D0 = DeselbyGround(lambdas)
    }


    fun setLambdas(lambda: (Agent) -> Double) {
        val lambdas = HashMap<Agent,Double>()
        for(pos in 0 until params.GRIDSIZESQ) {
            val pred = Predator(pos)
            val prey = Prey(pos)
            lambdas[pred] = lambda(pred)
            lambdas[prey] = lambda(prey)
        }
        D0 = DeselbyGround(lambdas)
    }


    fun calcFullHamiltonian(): FockVector<Agent> {
        val H = HashFockVector<Agent>()
        for(pos in 0 until params.GRIDSIZESQ) {
            Predator(pos).hamiltonian(H, params)
            Prey(pos).hamiltonian(H, params)
        }
        return H
    }


    fun monteCarloIntegrateParallel(startState: CreationBasis<Agent>, nSamples: Int, nThreads: Int, integrationTime: Double) : CreationVector<Agent> {
        val reducedHamiltonian = H * startState.asGroundedBasis(D0)

        val threadTotals = Array(nThreads) {
            GlobalScope.async {
                val total = HashCreationVector<Agent>()
                val possibleTransitionStates = SamplableDoubleVector(reducedHamiltonian)
                val threadQuota = nSamples.div(nThreads) + if(it < nSamples.rem(nThreads)) 1 else 0
                for(i in 1..threadQuota) {
                    val mcSample = startState.asGroundedBasis(D0).monteCarlo(hIndex, possibleTransitionStates, integrationTime)
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


    fun monteCarloIntegrate(startState: CreationBasis<Agent>, nSamples: Int, integrationTime: Double) : CreationVector<Agent> {
        val reducedHamiltonian = H * startState.asGroundedBasis(D0)
        println("Reduced hamiltonian size = ${reducedHamiltonian.size}")
        val total = HashCreationVector<Agent>()
        val possibleTransitionStates = SamplableDoubleVector(reducedHamiltonian)
        for(i in 1..nSamples) {
            val mcSample = startState.asGroundedBasis(D0).monteCarlo(hIndex, possibleTransitionStates, integrationTime)
            total += mcSample
//            if(i.rem(2000) == 0) println(mcSample)
        }
        return total / nSamples.toDouble()
    }
}