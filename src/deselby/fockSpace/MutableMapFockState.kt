package deselby.fockSpace

interface MutableMapFockState<AGENT> : MapFockState<AGENT>, MutableFockState<AGENT,MapFockState<AGENT>> {
    override val coeffs : MutableMap<FockBasis<AGENT>, Double>

    override operator fun set(b : FockBasis<AGENT>, value : Double) { coeffs[b] = value }

}

operator fun <AGENT> MutableMapFockState<AGENT>.plusAssign(entry : Map.Entry<FockBasis<AGENT>,Double>) {
    mergeRemoveIfZero(entry.key , entry.value, Double::plus)
}

operator fun <AGENT> MutableMapFockState<AGENT>.minusAssign(entry : Map.Entry<FockBasis<AGENT>,Double>) {
    mergeRemoveIfZero(entry.key , -entry.value, Double::plus)
}

inline fun <AGENT> MutableMapFockState<AGENT>.mergeRemoveIfZero(key : FockBasis<AGENT>, value :Double, crossinline op : (Double, Double) -> Double) : Double? {
    return coeffs.merge(key , value) {a , b ->
        val newVal = op(a, b)
        if(newVal == 0.0) null else newVal
    }
}
