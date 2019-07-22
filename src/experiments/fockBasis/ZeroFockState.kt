package experiments.fockBasis

class ZeroFockState<AGENT> : MapFockState<AGENT> {

    override val coeffs: Map<FockBasis<AGENT>, Double>
        get() = emptyMap()

    override fun unaryMinus(): ZeroFockState<AGENT> = this
    override fun create(a: AGENT, n : Int): ZeroFockState<AGENT> = this
    override fun create(creations: Map<AGENT, Int>): MapFockState<AGENT> = this
    override fun annihilate(d: AGENT): ZeroFockState<AGENT> = this
    override fun plus(other: MapFockState<AGENT>) = other
    override fun minus(other: MapFockState<AGENT>): MapFockState<AGENT> = other*(-1.0)
    override fun times(multiplier: Double): ZeroFockState<AGENT> = this
    override fun times(other: MapFockState<AGENT>): ZeroFockState<AGENT> = this
}

