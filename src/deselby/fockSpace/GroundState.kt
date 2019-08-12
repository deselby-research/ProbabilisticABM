package deselby.fockSpace

import deselby.fockSpace.extensions.LazyAnnihilationBasis
import deselby.std.vectorSpace.DoubleVector

interface GroundState<AGENT> {
    fun annihilate(d : AGENT) : DoubleVector<CreationBasis<AGENT>>
    fun lambda(d : AGENT) : Double
  //  fun annihilate(annihilations : Map<AGENT,Int>) : DoubleVector<CreationBasis<AGENT>>
    fun annihilate(annihilations : LazyAnnihilationBasis<AGENT>) : DoubleVector<CreationBasis<AGENT>>
}