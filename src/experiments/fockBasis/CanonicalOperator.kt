package experiments.fockBasis

class CanonicalOperator<AGENT>(override val coeffs: HashMap<FockBasis<AGENT>,Double> = HashMap())
    : AbstractMutableFockState<AGENT,CanonicalOperator<AGENT>>() {

    constructor(initialBasis : FockBasis<AGENT>) :this() { coeffs[initialBasis] = 1.0 }
    constructor(initialState : MapFockState<AGENT>) :this() { coeffs.putAll(initialState.coeffs) }

    override fun zero() = CanonicalOperator<AGENT>()

}