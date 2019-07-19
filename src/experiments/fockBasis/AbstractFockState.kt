package experiments.fockBasis

abstract class AbstractFockState<AGENT> : FockState<AGENT> {

    override fun zero() : AbstractMutableFockState<AGENT> = SparseFockDecomposition()

    override fun create(a : AGENT, n : Int) : FockState<AGENT> {
        val result = zero()
        coeffs.mapKeysTo(result.coeffs) {
            it.key.create(a, n)
        }
        return result
    }

    override fun create(a : AGENT) : FockState<AGENT> = create(a, 1)

    override fun annihilate(a : AGENT) : FockState<AGENT> {
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

    override fun toMutableFockState() : MutableFockState<AGENT> {
        val result = zero()
        result.coeffs.putAll(coeffs)
        return result
    }

    override operator fun get(b : FockBasis<AGENT>) = coeffs[b]?:0.0

    override operator fun plus(other : FockState<AGENT>) : FockState<AGENT> {
        val result = toMutableFockState()
        other.coeffs.forEach {
            result.coeffs.compute(it.key) {_, initValue ->
                val newVal = (initValue?:0.0) + it.value
                if(newVal == 0.0) null else newVal
            }
        }
        return result
    }

    override operator fun minus(other: FockState<AGENT>): FockState<AGENT> {
        val result = toMutableFockState()
        other.coeffs.forEach {
            result.coeffs.compute(it.key) {_, initValue ->
                val newVal = (initValue?:0.0) - it.value
                if(newVal == 0.0) null else newVal
            }
        }
        return result
    }

    override operator fun times(multiplier : Double) : FockState<AGENT> {
        val result = zero()
        if(multiplier != 0.0) {
            coeffs.mapValuesTo(result.coeffs) {
                it.value * multiplier
            }
        }
        return result
    }

    override operator fun times(other : FockState<AGENT>) : FockState<AGENT> {
        val result = zero()
        coeffs.forEach {term ->
            result += OneHotFock(term.key, term.value).times(other)
        }
        return result
    }

    fun integrate(hamiltonian : (FockState<AGENT>)-> FockState<AGENT>, T : Double, dt : Double) : FockState<AGENT> {
        val p  = toMutableFockState()
        var time = 0.0
        while(time < T) {
            p += hamiltonian(p)*dt
            time += dt
        }
        return p
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