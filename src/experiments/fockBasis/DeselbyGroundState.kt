package experiments.fockBasis

class DeselbyGroundState<AGENT>(val lambda : Map<AGENT, Double>) : FockBasis<AGENT> {
    override fun times(other: FockBasis<AGENT>): FockBasis<AGENT> {
        if(other is DeselbyGroundState<AGENT>) {
            return this
        } else if(other is PerturbedBasis<AGENT>){
            return other
        }
        throw(IllegalArgumentException("Multiplying incompatible bases"))
    }

    override fun create(d: AGENT, n: Int): FockBasis<AGENT> {
        if(n < 0) throw(IllegalArgumentException("Can't have removal operator on ground state"))
        return PerturbedBasis(mapOf(d to n), this)
    }

    override fun annihilate(d: AGENT): FockState<AGENT> {
        return OneHotFock(this, lambda[d]?:0.0)
    }

    override fun count(d: AGENT) = 0

    override fun toString() : String {
        return "0"
    }

    override fun hashCode(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        return other is DeselbyGroundState<*>
    }
}