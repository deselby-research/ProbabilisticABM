package deselby.fockSpace

// Given a canonical operator, S, will create a mapping from
// agent states, d, to Operators such that d -> a^-_d[a*_d,S]
class CreationCommutations<AGENT> : AbstractMap<AGENT, MapFockState<AGENT>> {
    val index = HashMap<AGENT, MutableMapFockState<AGENT>>()

    override val entries: Set<Map.Entry<AGENT, MapFockState<AGENT>>>
        get() = index.entries

    // implement commutation relation
    // [a*,a^m] = -ma^(m-1)
    // so
    // a^-_d[a*,a^m] = -m a^-_da^(m-1)
    constructor(stateToIndex : MapFockState<AGENT>) {
        stateToIndex.coeffs.entries.forEach { entry ->
            val basis = entry.key as OperatorBasis<AGENT>
            val indices = basis.annihilations.keys
            indices.forEach {d ->
                val annihilationsminus1d = HashMap(basis.annihilations)
                val m = annihilationsminus1d.merge(d, -1) {a,b ->
                    val sum = a+b
                    if(sum == 0) null else sum
                }?:0
                val creationsminus1d = HashMap(basis.creations)
                creationsminus1d.merge(d, -1) {a,b ->
                    val sum = a+b
                    if(sum == 0) null else sum
                }
                val mdminus1 = OperatorBasis(creationsminus1d, annihilationsminus1d)
                index.getOrPut(d, {SparseFockState()})[mdminus1] = entry.value*-(m+1)
            }
        }
    }


    override operator fun get(key : AGENT) = index[key]

}