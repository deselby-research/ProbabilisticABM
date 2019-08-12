package deselby.fockSpace

data class FockState<AGENT>(val creationVector: CreationVector<AGENT>, val ground: GroundState<AGENT>)

data class GroundBasis<AGENT>(val basis: CreationBasis<AGENT>, val ground: GroundState<AGENT>)

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

