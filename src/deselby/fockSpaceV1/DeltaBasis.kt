package deselby.fockSpaceV1

class DeltaBasis<AGENT>(override val creations : Map<AGENT,Int> = emptyMap()) : AbstractBasis<AGENT>() {

    override fun new(initCreations: Map<AGENT, Int>) = DeltaBasis(initCreations)

    override fun groundStateAnnihilate(d: AGENT): MapFockState<AGENT> {
        return ZeroFockState()
    }

    override fun times(other: FockBasis<AGENT>): MapFockState<AGENT> {
        if(other is DeltaBasis<AGENT>) {
            if(this == other) this.toFockState() else ZeroFockState()
        }
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}