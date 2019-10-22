package models.predatorPrey.fock

import deselby.fockSpace.BinomialBasis
import models.predatorPrey.Params
import models.predatorPrey.discreteEventModel.Simulation
import models.predatorPrey.toFockMap


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
