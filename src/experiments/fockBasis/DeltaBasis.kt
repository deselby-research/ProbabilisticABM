package experiments.fockBasis

class DeltaBasis<AGENT>(creations : Map<AGENT,Int> = HashMap()) : AbstractBasis<AGENT>(creations) {
    override fun new(creations: Map<AGENT, Int>) = DeltaBasis(creations)

    override fun groundStateAnnihilate(d: AGENT): MapFockState<AGENT> {
        return ZeroFockState()
    }

    override fun times(other: FockBasis<AGENT>): MapFockState<AGENT> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}