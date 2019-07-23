package deselby.fockSpace


open class SparseFockState<AGENT>(override val coeffs: HashMap<FockBasis<AGENT>,Double> = HashMap())
    : AbstractMutableFockState<AGENT,SparseFockState<AGENT>>() {

    constructor(initialBasis : FockBasis<AGENT>) :this() { coeffs[initialBasis] = 1.0 }

    constructor(initialState : MapFockState<AGENT>) :this() { coeffs.putAll(initialState.coeffs) }

    override fun zero(): SparseFockState<AGENT> {
        return SparseFockState()
    }
}

fun <AGENT> sparseFockStateOf(vararg coords : Pair<FockBasis<AGENT>,Double>) :
        SparseFockState<AGENT> {
    return(SparseFockState(hashMapOf(*coords)))
}
