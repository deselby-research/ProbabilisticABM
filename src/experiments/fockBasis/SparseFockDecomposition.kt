package experiments.fockBasis


open class SparseFockDecomposition<AGENT>(override val coeffs: HashMap<FockBasis<AGENT>,Double> = HashMap()) : AbstractMutableFockState<AGENT>() {
    constructor(initialBasis : FockBasis<AGENT>) :this() { coeffs[initialBasis] = 1.0 }
    constructor(initialState : AbstractFockState<AGENT>) :this() { coeffs.putAll(initialState.coeffs) }
}

fun <AGENT> sparseFockDecompositionOf(vararg coords : Pair<FockBasis<AGENT>,Double>) :
        SparseFockDecomposition<AGENT> {
    return(SparseFockDecomposition(hashMapOf(*coords)))
}
