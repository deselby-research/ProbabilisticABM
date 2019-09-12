package deselby.fockSpace

class ReflexiveBasis<AGENT>(creations: Map<AGENT, Int>, val d: AGENT) : Basis<AGENT>(creations) {

    override val annihilations: Map<AGENT,Int>
        get() = mapOf(d to 2)


    override fun create(entries: Iterable<Map.Entry<AGENT,Int>>) = ReflexiveBasis(creations * entries, d)


    // ca commuteToPerturbation C = (C^-1)c[a,C]
    override fun commuteToPerturbation(basis: CreationBasis<AGENT>, termConsumer: (Basis<AGENT>, Double) -> Unit) {
        val m = basis.creations[d]?:return
        termConsumer(ActionBasis(creations.times(d,-1), d), 2.0*m)
        if(m>1) termConsumer(CreationBasis(creations.times(d,-2)), (m*(m-1)).toDouble())
    }


    override fun create(d: AGENT, n: Int): Basis<AGENT> {
        return ReflexiveBasis(creations.times(d,n), d)
    }


    override fun timesAnnihilate(d: AGENT): Basis<AGENT> {
        throw(NotImplementedError())
    }


    override fun hashCode(): Int {
        return  1922 + creations.hashCode() + d.hashCode()*62
    }


    override fun equals(other: Any?): Boolean {
        if(other === this) return true
        if (other !is ReflexiveBasis<*>) return false
        return (d == other.d && creations == other.creations)
    }


    override fun toString(): String {
        return super.toString() + "a($d)^2"
    }
}