package deselby.fockSpace

import deselby.std.vectorSpace.DoubleVector
import deselby.std.vectorSpace.HashDoubleVector

class DeselbyPerturbation<AGENT>(val baseState : Deselby<AGENT>, override val creations : Map<AGENT,Int>) :
        AbstractBasis<AGENT, DeselbyPerturbation<AGENT>>() {

    constructor(base : Deselby<AGENT>) :this(base, emptyMap())

    override fun new(creations: MutableMap<AGENT, Int>)= DeselbyPerturbation(baseState, creations)

    override fun groundStateAnnihilate(d: AGENT): DoubleVector<DeselbyPerturbation<AGENT>> {
        val result = HashDoubleVector<DeselbyPerturbation<AGENT>>()
        baseState.creations[d]?.run {
            result[DeselbyPerturbation(baseState, mapOf(d to -1))] = this.toDouble()
        }
        baseState.lambda[d]?.run {
            result[DeselbyPerturbation(baseState, emptyMap())] = this
        }
        return result
    }

    override fun toString() : String {
        return "("+super.toString()+") + ("+baseState.toString()+")"
    }
}
