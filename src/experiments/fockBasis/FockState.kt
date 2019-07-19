package experiments.fockBasis

interface FockState<AGENT> : Fockable<AGENT> {
    val coeffs : Map<FockBasis<AGENT>, Double>

    fun zero() : MutableFockState<AGENT>
    fun toMutableFockState() : MutableFockState<AGENT>

    override fun create(a : AGENT, n : Int) : FockState<AGENT>
    override fun create(a : AGENT) : FockState<AGENT>
    override fun annihilate(a : AGENT) : FockState<AGENT>

    operator fun get(b : FockBasis<AGENT>) : Double
    operator fun plus(other : FockState<AGENT>) : FockState<AGENT>
    operator fun minus(other: FockState<AGENT>): FockState<AGENT>
    operator fun times(multiplier : Double) : FockState<AGENT>
    operator fun times(other : FockState<AGENT>) : FockState<AGENT>

}
