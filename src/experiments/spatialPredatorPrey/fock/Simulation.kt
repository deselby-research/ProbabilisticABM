package experiments.spatialPredatorPrey.fock

import deselby.fockSpace.*
import deselby.fockSpace.extensions.integrate
import deselby.fockSpace.extensions.times
import deselby.fockSpace.extensions.toAnnihilationIndex
import experiments.phasedMonteCarlo.monteCarlo
import experiments.spatialPredatorPrey.Params

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


    fun calcFullHamiltonian(): FockVector<Agent> {
        val H = HashFockVector<Agent>()
        for(pos in 0 until params.GRIDSIZESQ) {
            Predator(pos).hamiltonian(H, params)
            Prey(pos).hamiltonian(H, params)
        }
        return H
    }


    fun monteCarloIntegrate(startState: CreationBasis<Agent>, nSamples: Int, integrationTime: Double) : CreationVector<Agent> {
        val reducedHamiltonian = H * startState.asGroundedBasis(D0)
        val total = HashCreationVector<Agent>()
        for(i in 1..nSamples) {
            val mcSample = startState.asGroundedBasis(D0).monteCarlo(hIndex, reducedHamiltonian, integrationTime)
            total += mcSample
//            if(i.rem(2000) == 0) println(mcSample)
        }
        return total / nSamples.toDouble()
    }
}