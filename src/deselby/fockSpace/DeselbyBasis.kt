package deselby.fockSpace

class DeselbyBasis<AGENT>(val lambda : Map<AGENT, Double>, creations : Map<AGENT,Int> = HashMap()) : AbstractBasis<AGENT>(creations) {
    override fun new(creations: Map<AGENT, Int>) = DeselbyBasis(lambda, creations)

    override fun groundStateAnnihilate(d: AGENT): MapFockState<AGENT> {
        val ld = lambda[d]?:return ZeroFockState()
        return OneHotFock(DeselbyBasis(lambda), ld)
    }

    override fun times(other: FockBasis<AGENT>): MapFockState<AGENT> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}