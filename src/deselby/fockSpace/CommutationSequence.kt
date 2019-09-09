package deselby.fockSpace

// Calculates sequences of the form c_q = nAnnihilations!nCreations!/(q!(nAnnihilations-q)!(nCreations-q)!)
class CommutationSequence<AGENT>(val d: AGENT, val nAnnihilations: Int, val nCreations: Int) : Sequence<OperatorPair<AGENT>> {

    override fun iterator(): Iterator<OperatorPair<AGENT>> {
        return CoefficientIterator(d, nAnnihilations, nCreations, 1, 0)
    }

    class CoefficientIterator<AGENT>(val d: AGENT, val n: Int, val m: Int, var c: Int, var q: Int): Iterator<OperatorPair<AGENT>> {
        var isFirstItem = true

        override fun hasNext() = ((n > q) && (m > q))

        override fun next(): OperatorPair<AGENT> {
            if (isFirstItem) {
                isFirstItem = false
                return OperatorPair(d, c, m - q, n - q)
            }
            val nm = (n - q) * (m - q)
            ++q
            c = (c * nm) / q
            return OperatorPair(d, c, m - q, n - q)
        }
    }

}