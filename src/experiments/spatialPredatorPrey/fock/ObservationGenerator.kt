package experiments.spatialPredatorPrey.fock

import deselby.fockSpace.BinomialBasis
import deselby.fockSpace.BinomialLikelihood
import experiments.spatialPredatorPrey.Params
import experiments.spatialPredatorPrey.discreteEventABM.Simulation
import experiments.spatialPredatorPrey.toFockMap


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


}
