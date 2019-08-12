package deselby.fockSpace

import deselby.std.vectorSpace.DoubleVector
import deselby.std.vectorSpace.OneHotDoubleVector

class DeselbyGroundState<AGENT>(val lambdas : Map<AGENT,Double>) : GroundState<AGENT> {

    constructor(vararg lambdas: Pair<AGENT,Double>) :this(hashMapOf(*lambdas))

    override fun annihilate(d: AGENT): DoubleVector<CreationBasis<AGENT>> {
        return OneHotDoubleVector(Basis.identity(),lambdas[d]?:0.0)
    }

    override fun lambda(d : AGENT) = this.lambdas[d]?:0.0

}