package experiments.fockBasis

class ZeroFockState<AGENT> : AbstractFockState<AGENT>() {

    override val coeffs: Map<FockBasis<AGENT>, Double>
        get() = emptyMap()

    override fun create(a: AGENT, n : Int): FockState<AGENT> {
        return this
    }

    override fun annihilate(d: AGENT): FockState<AGENT> {
        return this
    }

    override fun plus(other: FockState<AGENT>): FockState<AGENT> {
        return(other)
    }

    override fun minus(other: FockState<AGENT>): FockState<AGENT> {
        return(other*(-1.0))
    }

    override fun times(multiplier: Double): FockState<AGENT> {
        return this
    }
}

