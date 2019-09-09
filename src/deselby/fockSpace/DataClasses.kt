package deselby.fockSpace

import deselby.std.vectorSpace.DoubleVector

data class GroundedVector<AGENT, out BASIS: Ground<AGENT>>(val creationVector: CreationVector<AGENT>, val ground: BASIS)

data class GroundedBasis<AGENT, out BASIS: Ground<AGENT>>(val basis: CreationBasis<AGENT>, val ground: BASIS) : Ground<AGENT> {

    //val identity = Basis.identity<AGENT>()

    override fun annihilate(d: AGENT): DoubleVector<CreationBasis<AGENT>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun lambda(d: AGENT): Double {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    // calculate this.basis^(-1)(lhsBasis * this.basis) * this.ground
    // = (lhsBasis + this.basis^(-1)[lhsBasis, this.basis]) * this.ground
    override fun preMultiply(lhsBasis: Basis<AGENT>, termConsumer: (CreationBasis<AGENT>, Double) -> Unit) {
        lhsBasis.multiply(ground, termConsumer)
        lhsBasis.commuteToPerturbation(basis) { basis, commutedWeight ->
            ground.preMultiply(basis) { creationBasis, groundWeight ->
                termConsumer(creationBasis, commutedWeight*groundWeight)
            }
        }
    }
}

data class OperatorPair<AGENT>(val d: AGENT, val weight: Int, val nCreations: Int, val nAnnihilations: Int)


