package deselby.fockSpace

import deselby.std.vectorSpace.DoubleVector
import deselby.std.vectorSpace.OneHotDoubleVector
import kotlin.math.pow

class DeselbyGroundState<AGENT>(val lambdas : Map<AGENT,Double>) : GroundState<AGENT> {

    override fun preMultiply(basis: Basis<AGENT>, termConsumer: (CreationBasis<AGENT>, Double) -> Unit) {
        var multiplier = 1.0
        basis.forEachAnnihilationEntry { d, n ->
            multiplier *= (lambdas[d]?:0.0).pow(n)
        }
        if(multiplier != 0.0) termConsumer(CreationBasis(basis.creations), multiplier)
    }

    override fun annihilate(d: AGENT): DoubleVector<CreationBasis<AGENT>> {
        return OneHotDoubleVector(Basis.identity(),lambdas[d]?:0.0)
    }

    override fun lambda(d : AGENT) = this.lambdas[d]?:0.0

}