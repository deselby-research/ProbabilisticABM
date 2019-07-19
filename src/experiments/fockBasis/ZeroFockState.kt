package experiments.fockBasis

class ZeroFockState<AGENT> : AbstractFockState<AGENT>() {

    override fun unaryMinus(): AbstractFockState<AGENT> {
        return this
    }

    override val coeffs: Map<FockBasis<AGENT>, Double>
        get() = emptyMap()

    override fun create(a: AGENT, n : Int): AbstractFockState<AGENT> {
        return this
    }

    override fun annihilate(d: AGENT): AbstractFockState<AGENT> {
        return this
    }

    override fun plus(other: AbstractFockState<AGENT>): AbstractFockState<AGENT> {
        return(other)
    }

    override fun minus(other: AbstractFockState<AGENT>): AbstractFockState<AGENT> {
        return(other*(-1.0))
    }

    override fun times(multiplier: Double): AbstractFockState<AGENT> {
        return this
    }
}

