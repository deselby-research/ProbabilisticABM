package deselby.fockSpaceV1

open class DeselbyBasis<AGENT>(val lambda : Map<AGENT, Double>, override val creations : Map<AGENT,Int> = emptyMap()) : AbstractBasis<AGENT>() {

    override fun new(initCreations: Map<AGENT, Int>) = DeselbyBasis(lambda, initCreations)

    override fun groundStateAnnihilate(d: AGENT): MapFockState<AGENT> {
        val ld = lambda[d]?:return ZeroFockState()
        return OneHotFock(DeselbyBasis(lambda), ld)
    }

    override fun times(other: FockBasis<AGENT>): MapFockState<AGENT> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}