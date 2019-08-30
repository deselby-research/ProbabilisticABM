package experiments.spatialPredatorPrey

import experiments.spatialPredatorPrey.fock.Agent
import experiments.spatialPredatorPrey.fock.Predator
import experiments.spatialPredatorPrey.fock.Prey


fun Map<experiments.spatialPredatorPrey.discreteEventABM.Agent,Int>.toFockMap() =
        mapKeys {(deAgent,_) -> deAgent.toFockAgent()}


fun experiments.spatialPredatorPrey.discreteEventABM.Agent.toFockAgent(): Agent {
    if(this is experiments.spatialPredatorPrey.discreteEventABM.Predator) return Predator(this.id)
    return Prey(this.id)
}
