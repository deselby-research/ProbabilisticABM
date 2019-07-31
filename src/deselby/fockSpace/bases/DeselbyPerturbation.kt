package deselby.fockSpace.bases

import deselby.std.vectorSpace.DoubleVector
import deselby.std.vectorSpace.HashMapDoubleVector

class DeselbyPerturbation<AGENT>(val baseState : Deselby<AGENT>, override val creations : Map<AGENT,Int>) :
        AbstractBasis<AGENT, DeselbyPerturbation<AGENT>>() {

    constructor(base : Deselby<AGENT>) :this(base, emptyMap())

    override fun new(creations: MutableMap<AGENT, Int>)= DeselbyPerturbation(baseState, creations)

    override fun groundStateAnnihilate(d: AGENT): DoubleVector<DeselbyPerturbation<AGENT>> {
        val result = HashMapDoubleVector<DeselbyPerturbation<AGENT>>()
        val base = baseState
        base.creations[d]?.run {
            result[DeselbyPerturbation(base, mapOf(d to -1))] = this.toDouble()
        }
        base.lambda[d]?.run {
            result[DeselbyPerturbation(base, emptyMap())] = this
        }
        return result
    }

    override fun toString() : String {
        return "("+super.toString()+") + ("+baseState.toString()+")"
    }
}
