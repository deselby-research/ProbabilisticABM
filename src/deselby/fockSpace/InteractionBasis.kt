package deselby.fockSpace

import java.io.Serializable

class InteractionBasis<AGENT> : Basis<AGENT>, Serializable {

    override val annihilations : Map<AGENT,Int>
        get() = mapOf(d1 to 1, d2 to 1)
    val d1: AGENT
    val d2: AGENT
    private val hashCache: Int


    constructor(creations: Map<AGENT, Int>, d1: AGENT, d2: AGENT) : super(creations) {
        if(d1 == d2) throw(IllegalArgumentException("InteractionBasis should involve different agents. Use ReflexiveBasis instead"))
        this.d1 = d1
        this.d2 = d2
        hashCache = 1922 + creations.hashCode() + (d1.hashCode() + d2.hashCode())*31
    }


    override fun create(entries: Iterable<Map.Entry<AGENT,Int>>) = InteractionBasis(creations * entries, d1, d2)


    // ca commuteToPerturbation C = (C^-1)[a,C]
    override fun commuteToPerturbation(basis: CreationBasis<AGENT>, termConsumer: (Basis<AGENT>, Double) -> Unit) {
        val m1 = basis.creations[d1]
        val m2 = basis.creations[d2]
        if(m1 != null) {
            if(m2 != null) {
                termConsumer(ActionBasis(mapOf(d1 to -1), d2), m1.toDouble())
                termConsumer(ActionBasis(mapOf(d2 to -1), d1), m2.toDouble())
                termConsumer(CreationBasis(mapOf(d1 to -1, d2 to -1)), (m1*m2).toDouble())
            } else {
                termConsumer(ActionBasis(mapOf(d1 to -1), d2), m1.toDouble())
            }
        } else if(m2 != null) {
            termConsumer(ActionBasis(mapOf(d2 to -1), d1), m2.toDouble())
        }
    }


    override fun create(d: AGENT, n: Int): Basis<AGENT> {
        return InteractionBasis(creations.times(d,n), d1, d2)
    }


    override fun timesAnnihilate(d: AGENT): Basis<AGENT> {
        val annihilations = HashMap<AGENT,Int>()
        annihilations[d1] = 1
        annihilations[d2] = 1
        annihilations.timesAssign(d, 1)
        return OperatorBasis(creations, annihilations)
    }

    override fun hashCode(): Int {
        return hashCache
    }


    override fun equals(other: Any?): Boolean {
        if(other === this) return true
        if (other !is InteractionBasis<*>) return false
        return (((d1 == other.d1 && d2 == other.d2) || (d1 == other.d2 && d2 == other.d1))
                && creations == other.creations)
    }


    override fun toString(): String {
        return super.toString() + "a($d1)a($d2)"
    }
}