package deselby.fockSpace

import java.io.Serializable

class ActionBasis<AGENT>(creations: Map<AGENT, Int>, val d: AGENT) : Basis<AGENT>(creations), Serializable {
    val hashCache: Int = 961 + creations.hashCode() + d.hashCode()*31

    override val annihilations: Map<AGENT, Int>
        get() = mapOf(d to 1)

    constructor(creations: Collection<AGENT>, annihilation: AGENT) : this(creations.toCountMap(), annihilation)


    // Let this = ca, ca commuteToPerturbation C = (C^-1)[a,C]
    // = nCreations a_d*^(-1)
    // since [a_d, a_d*^nCreations] = nCreations a_d*^(nCreations-1)
    override fun commuteToPerturbation(basis: CreationBasis<AGENT>, termConsumer: (Basis<AGENT>, Double) -> Unit) {
        val m = basis.creations[d]?:return
        termConsumer(CreationBasis(mapOf(d to -1)), m.toDouble())
    }


    override fun create(entries: Iterable<Map.Entry<AGENT,Int>>) = ActionBasis(creations * entries, d)


    override fun create(d: AGENT, n: Int): Basis<AGENT> {
        return ActionBasis(creations.times(d,n), d)
    }

    override fun timesAnnihilate(d: AGENT): Basis<AGENT> {
        return if(this.d == d) ReflexiveBasis(creations, d) else InteractionBasis(creations, this.d, d)
    }

    override fun hashCode(): Int {
        return hashCache
    }


    override fun equals(other: Any?): Boolean {
        if(other === this) return true
        if (other !is ActionBasis<*>) return false
        return (d == other.d && creations == other.creations)
    }


    override fun toString(): String {
        return super.toString() + "a($d)"
    }

}