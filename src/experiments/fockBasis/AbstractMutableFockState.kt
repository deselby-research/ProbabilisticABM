package experiments.fockBasis

abstract class AbstractMutableFockState<AGENT> : AbstractFockState<AGENT>(), MutableFockState<AGENT> {
    override operator fun timesAssign(multiplier: Double) {
        if(multiplier == 0.0) coeffs.clear()
        coeffs.entries.forEach {
            it.setValue(it.value * multiplier)
        }
    }

    override operator fun plusAssign(other: FockState<AGENT>) {
        other.coeffs.forEach {
            coeffs.compute(it.key) {_, initValue ->
                val newVal = (initValue?:0.0) + it.value
                if(newVal == 0.0) null else newVal
            }
        }
    }

    override operator fun minusAssign(other: FockState<AGENT>) {
        other.coeffs.forEach {
            coeffs.compute(it.key) {_, initValue ->
                val newVal = (initValue?:0.0) - it.value
                if(newVal == 0.0) null else newVal
            }
        }
    }

    override operator fun set(b : FockBasis<AGENT>, value : Double) {
        coeffs[b] = value
    }

    override fun setToZero() {
        coeffs.clear()
    }

}