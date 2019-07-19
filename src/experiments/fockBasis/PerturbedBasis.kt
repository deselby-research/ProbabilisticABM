package experiments.fockBasis

class PerturbedBasis<AGENT>(val perturbations : Map<AGENT, Int>, val baseState : FockBasis<AGENT>) : FockBasis<AGENT> {

    override fun times(other: FockBasis<AGENT>): FockBasis<AGENT> {
        if(other !is PerturbedBasis<AGENT>) throw(IllegalArgumentException("Multiplying incompatible bases"))
        val newPerts = HashMap(perturbations)
        other.perturbations.forEach { newPerts.merge(it.key, it.value, Int::plus) }
        return PerturbedBasis(newPerts, baseState*other.baseState)
    }


    fun apply(perturbation: Map<AGENT, Int>): FockBasis<AGENT> {
        val newPerts = HashMap(perturbations)
        perturbation.forEach { newPerts.merge(it.key, it.value, Int::plus) }
        return PerturbedBasis(newPerts, baseState)
    }


    override fun create(d: AGENT, n : Int): FockBasis<AGENT> {
        val delta = HashMap(perturbations)
        delta.compute(d) {_, initialCount ->
            val newCount = (initialCount?:0) + n
            if(newCount == 0) null else newCount
        }
        if(delta.size == 0) return baseState
        return PerturbedBasis(delta, baseState)
    }


    // using the identity
    // aa*^n = a*^(n-1)(n + a*a)
    // for n != 0
    override fun annihilate(d: AGENT): FockState<AGENT> {
        val nd = perturbations[d]?:0
        if(nd == 0) return applyTo(baseState.annihilate(d))
        return this.remove(d)*nd.toDouble() + applyTo(baseState.annihilate(d))
    }


    override fun count(d: AGENT): Int {
        return baseState.count(d) + perturbations.getOrDefault(d,0)
    }


    override fun toString() : String {
        var s = ""
        perturbations.forEach {
            s += "${it.key}:${it.value} "
        }
        return s
    }


    // applies this perturbation to the given state rather than
    // the base state
    private fun applyTo(state : FockState<AGENT>) : FockState<AGENT> {
        val result = SparseFockDecomposition<AGENT>()
        state.coeffs.mapKeysTo(result.coeffs) {
            val unperturbedBasis = it.key
            if(unperturbedBasis is PerturbedBasis<AGENT>) {
                unperturbedBasis.apply(perturbations)
            } else {
                PerturbedBasis(perturbations, unperturbedBasis)
            }
        }
        return result
    }


    override fun hashCode(): Int {
        return perturbations.hashCode()
    }


    override fun equals(other: Any?): Boolean {
        if(super.equals(other)) return true
        if(other !is PerturbedBasis<*>) return false
        return perturbations == other.perturbations && baseState == other.baseState
    }
}