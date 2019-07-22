package experiments.fockBasis

import deselby.std.distributions.AbsMutableCategorical
import kotlin.math.sign

class SamplableFockState<AGENT>(override val coeffs: AbsMutableCategorical<FockBasis<AGENT>> = AbsMutableCategorical()) : AbstractMutableFockState<AGENT,SamplableFockState<AGENT>>() {

    constructor(initialBasis : FockBasis<AGENT>) :this() { coeffs[initialBasis] = 1.0 }
    constructor(initialState : MapFockState<AGENT>) :this() { coeffs.putAll(initialState.coeffs) }

    override fun zero() = SamplableFockState<AGENT>()

    fun sample() : OneHotFock<AGENT> {
        val basis = coeffs.sample()
        return OneHotFock(basis, coeffs[basis].sign)
    }
}
