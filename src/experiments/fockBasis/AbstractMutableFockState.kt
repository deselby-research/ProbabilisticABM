package experiments.fockBasis

abstract class AbstractMutableFockState<AGENT,STATE : MutableMapFockState<AGENT>> : MutableMapFockState<AGENT> {
    abstract override val coeffs : MutableMap<FockBasis<AGENT>, Double>

    abstract fun zero() : STATE

    override fun create(a : AGENT, n : Int) : STATE {
        val result = zero()
        coeffs.mapKeysTo(result.coeffs) {
            it.key.create(a, n)
        }
        return result
    }

    override fun create(a : AGENT)  : STATE {
        val result = zero()
        coeffs.mapKeysTo(result.coeffs) {
            it.key.create(a)
        }
        return result
    }

    override fun create(creations: Map<AGENT, Int>): STATE {
        val result = zero()
        coeffs.mapKeysTo(result.coeffs) {
            it.key.create(creations)
        }
        return result
    }

    override fun annihilate(a : AGENT) : STATE {
        val result = zero()
        coeffs.forEach { monomial ->
            monomial.key.annihilate(a).coeffs.forEach { annihilatedMonomial ->
                result.mergeRemoveIfZero(
                        annihilatedMonomial.key ,
                        annihilatedMonomial.value*monomial.value,
                        Double::plus
                )
            }
        }
        return result
    }

    fun toMutableFockState() : STATE {
        val result = zero()
        result.coeffs.putAll(coeffs)
        return result
    }

    override operator fun plus(other : MapFockState<AGENT>) : STATE {
        val result = toMutableFockState()
        other.coeffs.forEach { result += it }
        return result
    }

    override operator fun minus(other: MapFockState<AGENT>): STATE {
        val result = toMutableFockState()
        other.coeffs.forEach { result -= it }
        return result
    }


    override operator fun times(multiplier : Double) : STATE {
        val result = zero()
        if(multiplier != 0.0) {
            coeffs.mapValuesTo(result.coeffs) {
                it.value * multiplier
            }
        }
        return result
    }

    override operator fun times(other : MapFockState<AGENT>) : STATE {
        val result = zero()
        coeffs.forEach {term ->
            result += OneHotFock(term.key, term.value).times(other)
        }
        return result
    }

    override operator fun timesAssign(multiplier: Double) {
        if(multiplier == 0.0) coeffs.clear()
        coeffs.entries.forEach {
            it.setValue(it.value * multiplier)
        }
    }

    override operator fun plusAssign(other: MapFockState<AGENT>) {
        other.coeffs.forEach { this += it }
    }

    override operator fun minusAssign(other: MapFockState<AGENT>) {
        other.coeffs.forEach { this -= it }
    }

    override operator fun timesAssign(other : MapFockState<AGENT>) {
        val result = this * other
        setToZero()
        coeffs.putAll(result.coeffs)
    }


    override fun setToZero() {
        coeffs.clear()
    }

    override fun toString() : String {
        if(coeffs.isEmpty()) return "{}"
        var s = ""
        coeffs.forEach {
            s += "%+fP[%s] ".format(it.value, it.key)// ""${it.value}P[${it.key}] "
        }
        return s
    }
}