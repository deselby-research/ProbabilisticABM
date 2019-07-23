package deselby.fockSpace

class OneHotFock<AGENT>(val basis : FockBasis<AGENT>, val probability : Double = 1.0) : MapFockState<AGENT> {

    override val coeffs = mapOf(basis to probability)

    override fun unaryMinus(): OneHotFock<AGENT> = OneHotFock(basis, -probability)

    override fun create(d: AGENT, n : Int): OneHotFock<AGENT> = OneHotFock(basis.create(d, n), probability)

    override fun create(d: AGENT): OneHotFock<AGENT> = OneHotFock(basis.create(d), probability)

    override fun create(creations: Map<AGENT, Int>): OneHotFock<AGENT> = OneHotFock(basis.create(creations),probability)

    override fun annihilate(d: AGENT): MapFockState<AGENT> = basis.annihilate(d) * probability

    override operator fun plus(other: MapFockState<AGENT>): SparseFockState<AGENT> {
        val result = SparseFockState(other)
        result.mergeRemoveIfZero(basis, probability, Double::plus)
        return result
    }

    // result = -other + this
    override operator fun minus(other: MapFockState<AGENT>): SparseFockState<AGENT> {
        val result = SparseFockState<AGENT>()
        other.coeffs.mapValuesTo(result.coeffs) {-1.0 * it.value}
        result.mergeRemoveIfZero(basis, probability, Double::plus)
        return result
    }

    override operator fun times(multiplier: Double): OneHotFock<AGENT> = OneHotFock(basis, probability*multiplier)

    override operator fun times(other : MapFockState<AGENT>) : SparseFockState<AGENT> {
        val result = SparseFockState<AGENT>()
        other.coeffs.forEach { otherEntry ->
            result += (basis*otherEntry.key)*(otherEntry.value*probability)
        }
        return result
    }

    override fun toString(): String {
        return "%+fP[%s]".format(probability, basis)
    }
}