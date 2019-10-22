package models.predatorPrey

import models.predatorPrey.fock.Agent
import models.predatorPrey.fock.Predator
import models.predatorPrey.fock.Prey


fun Map<models.predatorPrey.discreteEventModel.Agent,Int>.toFockMap() =
        mapKeys {(deAgent,_) -> deAgent.toFockAgent()}


fun models.predatorPrey.discreteEventModel.Agent.toFockAgent(): Agent {
    if(this is models.predatorPrey.discreteEventModel.Predator) return Predator(this.id)
    return Prey(this.id)
}
