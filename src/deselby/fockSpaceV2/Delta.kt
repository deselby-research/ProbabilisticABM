package deselby.fockSpaceV2

import deselby.std.vectorSpace.DoubleVector
import deselby.std.vectorSpace.EmptyDoubleVector

class Delta<AGENT>(override val creations : Map<AGENT,Int> = emptyMap()) : AbstractBasis<AGENT, Delta<AGENT>>() {

    override fun new(initCreations: MutableMap<AGENT, Int>) = Delta(initCreations)

    override fun groundStateAnnihilate(d: AGENT): DoubleVector<Delta<AGENT>> {
        return EmptyDoubleVector()
    }
}