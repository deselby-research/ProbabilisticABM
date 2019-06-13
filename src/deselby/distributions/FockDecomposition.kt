package deselby.distributions

class FockDecomposition<AGENT, BASIS: FockBasis<AGENT, BASIS>>(val coeffs: HashMap<BASIS, Double>)
    : FockState<AGENT,FockDecomposition<AGENT,BASIS>> {

    override fun create(d: AGENT): FockDecomposition<AGENT, BASIS> {
        val result = FockDecomposition<AGENT,BASIS>(HashMap())
        coeffs.forEach { basisDist ->
            val basisCreate = basisDist.key.create(d)
            basisCreate.coeffs.forEach { basis ->
                result.coeffs.merge(basis.key, basis.value, Double::plus)
            }
        }
        return result
    }

    override fun annihilate(d: AGENT): FockDecomposition<AGENT, BASIS> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun plus(other: FockDecomposition<AGENT, BASIS>): FockDecomposition<AGENT, BASIS> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun minus(other: FockDecomposition<AGENT, BASIS>): FockDecomposition<AGENT, BASIS> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun times(const: Double): FockDecomposition<AGENT, BASIS> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}