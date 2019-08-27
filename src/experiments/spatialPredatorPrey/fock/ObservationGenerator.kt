package experiments.spatialPredatorPrey.fock

import deselby.fockSpace.BinomialBasis
import deselby.fockSpace.BinomialLikelihood
import experiments.spatialPredatorPrey.Params
import experiments.spatialPredatorPrey.discreteEventABM.Simulation


object ObservationGenerator {

    fun generate(params: Params, pObserve: Double, nObservations: Int, obsInterval: Double = 1.0): List<Observation> {
        val sim = Simulation(params)
        val deObservations = sim.generateObservations(pObserve, nObservations, obsInterval)
        return deObservations.map {(realDeState, observedDeState) ->
            Observation(
                    realDeState.toFockMap(),
                    BinomialBasis(pObserve, observedDeState.toFockMap())
            )
        }
    }


    fun Map<experiments.spatialPredatorPrey.discreteEventABM.Agent,Int>.toFockMap() =
            mapKeys {(deAgent,_) -> deAgent.toFockAgent()}


    fun experiments.spatialPredatorPrey.discreteEventABM.Agent.toFockAgent(): Agent {
        if(this is experiments.spatialPredatorPrey.discreteEventABM.Predator) return Predator(this.id)
        return Prey(this.id)
    }
}