package experiments.fockBasis

import deselby.std.distributions.AbsMutableCategorical
import kotlin.math.sign

class SamplableFockDecomposition<AGENT>(override val coeffs: AbsMutableCategorical<FockBasis<AGENT>> = AbsMutableCategorical()) : AbstractMutableFockState<AGENT>() {
    constructor(initialBasis : FockBasis<AGENT>) :this() { coeffs[initialBasis] = 1.0 }
    constructor(initialState : FockState<AGENT>) :this() { coeffs.putAll(initialState.coeffs) }

    fun sample() : OneHotFock<AGENT> {
        val basis = coeffs.sample()
        return OneHotFock(basis, coeffs[basis].sign)
    }
}
