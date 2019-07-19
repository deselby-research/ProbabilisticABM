package experiments.spatialPredatorPrey.generatorPoly

import deselby.distributions.FockState

abstract class Interaction<AGENT>(rate : Double) : Act<AGENT>(rate) {
    abstract fun objectSelector(a: AGENT) : Sequence<AGENT>
    abstract operator fun <FOCKSTATE : FockState<AGENT, FOCKSTATE>> invoke(subj : AGENT, obj : AGENT, pabm :FOCKSTATE) : FOCKSTATE
}
