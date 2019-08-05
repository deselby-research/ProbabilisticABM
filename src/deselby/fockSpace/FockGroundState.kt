package deselby.fockSpace

import deselby.std.vectorSpace.DoubleVector

interface FockGroundState<AGENT> {
    fun annihilate(d : AGENT) : DoubleVector<CreationBasis<AGENT>>
}