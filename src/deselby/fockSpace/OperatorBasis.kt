package deselby.fockSpace

import deselby.std.combinatorics.combinations
import java.lang.IllegalArgumentException

class OperatorBasis<AGENT> : Basis<AGENT> {

    override val annihilations: Map<AGENT, Int>

    constructor(creations: Map<AGENT, Int>, annihilations: Map<AGENT, Int>) : super(creations) {
        if(annihilations.values.sum() < 3) throw(IllegalArgumentException("Number of annihilations should be more than 2 in an OperatorBasis. Try using Basis.newBasis instead."))
        this.annihilations = annihilations
    }

    override fun create(entries: Iterable<Map.Entry<AGENT, Int>>) = OperatorBasis(creations * entries, annihilations)

    override fun create(d: AGENT, n: Int) = OperatorBasis(creations.times(d,n), annihilations)

    override fun timesAnnihilate(d: AGENT) = OperatorBasis(creations, annihilations.times(d,1))

    // ca commuteToPerturbation C = (C^-1)c[a,C]
    override fun commuteToPerturbation(basis: CreationBasis<AGENT>, termConsumer: (Basis<AGENT>, Double) -> Unit) {
        val annihilationIterator = annihilations.iterator()
        val commutationSequences = Array(annihilations.size) {
            val (agent, nAnnihilations) = annihilationIterator.next()
            CommutationSequence(agent, nAnnihilations, basis.creations[agent] ?: 0).asIterable()
        }.asList()
        commutationSequences.combinations().drop(1).forEach { combination ->
            val termAnnihilations = HashMap<AGENT, Int>()
            val termCreations = HashMap<AGENT, Int>()
            var weight = 1.0
            combination.forEach { caPair ->
                val nOtherCreations = basis[caPair.d]
                if(caPair.nCreations != nOtherCreations) termCreations[caPair.d] = caPair.nCreations - nOtherCreations
                if(caPair.nAnnihilations != 0) termAnnihilations[caPair.d] = caPair.nAnnihilations
                weight *= caPair.weight
            }
            val termBasis = newBasis(creations * termCreations, termAnnihilations)
            termConsumer(termBasis, weight)
        }
    }


    override fun hashCode(): Int {
        return  creations.hashCode() + 31*annihilations.hashCode()
    }


    override fun equals(other: Any?): Boolean {
        if(other === this) return true
        if (other !is OperatorBasis<*>) return false
        return (creations == other.creations && annihilations == other.annihilations)
    }


    override fun toString(): String {
        return buildString {
            append(super.toString())
            for (c in annihilations) {
                if (c.value == 1) append("a(${c.key})") else append("a(${c.key})^${c.value}")
            }
        }
    }

}