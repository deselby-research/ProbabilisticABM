package deselby.fockSpaceV1

class DeselbyPerturbationBasis<AGENT>(val baseState : DeselbyBasis<AGENT>, override val creations : Map<AGENT,Int>) :
        AbstractBasis<AGENT>() {

    constructor(base : DeselbyBasis<AGENT>) :this(base, emptyMap())

    override fun new(initCreations: Map<AGENT, Int>): AbstractBasis<AGENT> {
        return DeselbyPerturbationBasis(baseState, initCreations)
    }

    override fun groundStateAnnihilate(d: AGENT): MapFockState<AGENT> {
        val result = SparseFockState<AGENT>()
        val base = baseState
        base.creations[d]?.run {
            result.coeffs[DeselbyPerturbationBasis(base, mapOf(d to -1))] = this.toDouble()
        }
        base.lambda[d]?.run {
            result.coeffs[DeselbyPerturbationBasis(base, emptyMap())] = this
        }
        return result
    }

    override operator fun times(other: FockBasis<AGENT>): MapFockState<AGENT> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toString() : String {
        return "("+super.toString()+") + ("+baseState.toString()+")"
    }
}