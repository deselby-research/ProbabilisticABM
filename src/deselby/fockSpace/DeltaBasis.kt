package deselby.fockSpace

class DeltaBasis<AGENT>(override val creations : Map<AGENT,Int> = emptyMap()) : AbstractBasis<AGENT>() {

    override fun new(creations: Map<AGENT, Int>) = DeltaBasis(creations)

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