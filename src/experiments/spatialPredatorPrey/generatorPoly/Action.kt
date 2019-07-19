package experiments.spatialPredatorPrey.generatorPoly

import deselby.distributions.FockState

abstract class Action<AGENT>(rate : Double) : Act<AGENT>(rate) {
    abstract operator fun <FOCKSTATE: FockState<AGENT,FOCKSTATE>> invoke(subj : AGENT, pabm : FOCKSTATE) : FOCKSTATE
}