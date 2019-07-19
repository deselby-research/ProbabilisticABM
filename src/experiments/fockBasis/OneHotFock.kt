package experiments.fockBasis

import kotlin.math.sign

class OneHotFock<AGENT>(var basis : FockBasis<AGENT>, var probability : Double) : AbstractFockState<AGENT>() {

    override fun unaryMinus(): AbstractFockState<AGENT> {
        return OneHotFock(basis, -probability)
    }

    override val coeffs = mapOf(basis to probability)

    override fun create(a: AGENT): OneHotFock<AGENT> {
        return OneHotFock(basis.create(a), probability)
    }

    override fun annihilate(a: AGENT): AbstractFockState<AGENT> {
        return basis.annihilate(a) * probability
    }

    override operator fun plus(other: AbstractFockState<AGENT>): AbstractFockState<AGENT> {
        val result = other.toMutableFockState()
        result.coeffs.compute(basis) {_, initValue ->
            val newVal = probability + (initValue?:0.0)
            if(newVal == 0.0) null else newVal
        }
        return result
    }

    // result = -other + this
    override operator fun minus(other: AbstractFockState<AGENT>): AbstractFockState<AGENT> {
        val result = zero()
        other.coeffs.mapValuesTo(result.coeffs) {-1.0 * it.value}
        result.coeffs.compute(basis) {_, initValue ->
            val newVal = probability + (initValue?:0.0)
            if(newVal == 0.0) null else newVal
        }
        return result
    }

    override operator fun times(multiplier: Double): OneHotFock<AGENT> {
        return OneHotFock(basis, probability*multiplier)
    }

    override operator fun times(other : AbstractFockState<AGENT>) : AbstractFockState<AGENT> {
        val result = SparseFockDecomposition<AGENT>()
        other.coeffs.forEach {
            result.coeffs[it.key*basis] = it.value * probability
        }
        return result
    }
}