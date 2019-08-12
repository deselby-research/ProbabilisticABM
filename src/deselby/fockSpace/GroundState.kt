package deselby.fockSpace

import deselby.std.vectorSpace.DoubleVector

interface GroundState<AGENT> {
    fun annihilate(d : AGENT) : DoubleVector<CreationBasis<AGENT>>
    fun lambda(d : AGENT) : Double
}