package experiments.fockBasis

abstract class AbstractFockState<AGENT> : FockState<AGENT, AbstractFockState<AGENT>> {
    abstract val coeffs : Map<FockBasis<AGENT>, Double>

    fun zero() : AbstractMutableFockState<AGENT> = SparseFockDecomposition()

    override fun create(a : AGENT, n : Int) : AbstractFockState<AGENT> {
        val result = zero()
        coeffs.mapKeysTo(result.coeffs) {
            it.key.create(a, n)
        }
        return result
    }

    override fun create(a : AGENT) : AbstractFockState<AGENT> = create(a, 1)

    override fun annihilate(a : AGENT) : AbstractFockState<AGENT> {
        val result = zero()
        coeffs.forEach { monomial ->
            monomial.key.annihilate(a).coeffs.forEach { annihilatedMonomial ->
                result.coeffs.compute(annihilatedMonomial.key) {_, initValue ->
                    val newVal = (initValue?:0.0) + annihilatedMonomial.value*monomial.value
                    if(newVal == 0.0) null else newVal
                }
            }
        }
        return result
    }

    fun toMutableFockState() : AbstractMutableFockState<AGENT> {
        val result = zero()
        result.coeffs.putAll(coeffs)
        return result
    }

    operator fun get(b : FockBasis<AGENT>) = coeffs[b]?:0.0

    override operator fun plus(other : AbstractFockState<AGENT>) : AbstractFockState<AGENT> {
        val result = toMutableFockState()
        other.coeffs.forEach {
            result.coeffs.compute(it.key) {_, initValue ->
                val newVal = (initValue?:0.0) + it.value
                if(newVal == 0.0) null else newVal
            }
        }
        return result
    }

    override operator fun minus(other: AbstractFockState<AGENT>): AbstractFockState<AGENT> {
        val result = toMutableFockState()
        other.coeffs.forEach {
            result.coeffs.compute(it.key) {_, initValue ->
                val newVal = (initValue?:0.0) - it.value
                if(newVal == 0.0) null else newVal
            }
        }
        return result
    }

    override operator fun unaryMinus(): AbstractFockState<AGENT> {
        return(this * (-1.0))
    }

    override operator fun times(multiplier : Double) : AbstractFockState<AGENT> {
        val result = zero()
        if(multiplier != 0.0) {
            coeffs.mapValuesTo(result.coeffs) {
                it.value * multiplier
            }
        }
        return result
    }

    override operator fun times(other : AbstractFockState<AGENT>) : AbstractFockState<AGENT> {
        val result = zero()
        coeffs.forEach {term ->
            result += OneHotFock(term.key, term.value).times(other)
        }
        return result
    }


    override fun toString() : String {
        if(coeffs.isEmpty()) return "{}"
        var s = ""
        coeffs.forEach {
            s += "${it.value}P[${it.key}] "
        }
        return s
    }


//    fun apply(perturbation: Map<AGENT, Int>): FockState<AGENT> {
//        val result = zero()
//        coeffs.mapKeysTo(result.coeffs) { it.key.apply(perturbation) }
//        return result
//    }
}