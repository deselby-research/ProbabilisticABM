package deselby.fockSpace

import deselby.std.vectorSpace.DoubleVector

data class FockState<AGENT, out BASIS: GroundState<AGENT>>(val creationVector: CreationVector<AGENT>, val ground: BASIS)

data class GroundBasis<AGENT, out BASIS: GroundState<AGENT>>(val basis: CreationBasis<AGENT>, val ground: BASIS) : GroundState<AGENT> {
    val identity = Basis.identity<AGENT>()

    override fun annihilate(d: AGENT): DoubleVector<CreationBasis<AGENT>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun lambda(d: AGENT): Double {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun preMultiply(lhsBasis: Basis<AGENT>, termConsumer: (CreationBasis<AGENT>, Double) -> Unit) {
        lhsBasis.multiply(ground, termConsumer)
        lhsBasis.commuteToPerturbation(basis) { basis, commutedWeight ->
            ground.preMultiply(basis) { creationBasis, groundWeight ->
                termConsumer(creationBasis, commutedWeight*groundWeight)
            }
        }
    }
}

data class CommutationCoefficient(val n: Int, val m: Int, val c: Int, val q: Int) {
    fun next() : CommutationCoefficient? {
        val newq = q+1
        val newState = CommutationCoefficient(n-1, m-1, c*n*m/newq, newq)
        return if(newState.c == 0) null else newState
    }

    companion object {
        fun getStandardForm(n: Int, m: Int) =
                generateSequence(CommutationCoefficient(n, m, 1, 0), CommutationCoefficient::next)

        fun getCommutation(n: Int, m: Int) =
                generateSequence(CommutationCoefficient(n-1, m-1, n*m, 1), CommutationCoefficient::next)
    }
}

