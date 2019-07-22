package experiments.fockBasis

abstract class PerturbedBasis<AGENT>(val perturbations : Map<AGENT, Int>, val baseState : FockBasis<AGENT>) : FockBasis<AGENT> {

//    override fun times(other: FockBasis<AGENT>): MapFockState<AGENT> {
//        if(other !is PerturbedBasis<AGENT>) throw(IllegalArgumentException("Multiplying incompatible bases"))
//        val newPerts = HashMap(creations)
//        other.creations.forEach { newPerts.mergeRemoveIfZero(it.key, it.value, Int::plus) }
//        return PerturbedBasis(newPerts, baseState*other.baseState)
//    }

    abstract fun new(perturbations : Map<AGENT, Int>, baseState : FockBasis<AGENT>) : PerturbedBasis<AGENT>

    fun apply(perturbation: Map<AGENT, Int>): FockBasis<AGENT> {
        val newPerts = HashMap(perturbations)
        perturbation.forEach { newPerts.merge(it.key, it.value, Int::plus) }
        return new(newPerts, baseState)
    }


    override fun create(d: AGENT, n : Int): FockBasis<AGENT> {
        val delta = HashMap(perturbations)
        delta.compute(d) {_, initialCount ->
            val newCount = (initialCount?:0) + n
            if(newCount == 0) null else newCount
        }
        if(delta.size == 0) return baseState
        return new(delta, baseState)
    }


    // using the identity
    // aa*^n = a*^(n-1)(n + a*a)
    // for n != 0
    override fun annihilate(d: AGENT): MapFockState<AGENT> {
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
        return s.dropLast(1)
    }


    // applies this perturbation to the given state rather than
    // the base state
    private fun applyTo(state : MapFockState<AGENT>) : MapFockState<AGENT> {
        val result = SparseFockState<AGENT>()
        state.coeffs.mapKeysTo(result.coeffs) {
            val unperturbedBasis = it.key
            if(unperturbedBasis is PerturbedBasis<AGENT>) {
                unperturbedBasis.apply(perturbations)
            } else {
                new(perturbations, unperturbedBasis)
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