package experiments.fockBasis

interface MutableFockState<AGENT> : FockState<AGENT> {
    override val coeffs : MutableMap<FockBasis<AGENT>, Double>

    operator fun timesAssign(multiplier: Double)
    operator fun plusAssign(other: FockState<AGENT>)
    operator fun minusAssign(other: FockState<AGENT>)
    operator fun set(b : FockBasis<AGENT>, value : Double)
    fun setToZero()

}