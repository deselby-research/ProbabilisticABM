package deselby.fockSpace

import java.lang.IllegalArgumentException

class OperatorBasis<AGENT> : Basis<AGENT> {

    val annihilations: Map<AGENT, Int>

    constructor(creations: Map<AGENT, Int>, annihilations: Map<AGENT, Int>) : super(creations) {
        if(annihilations.values.sum() < 3) throw(IllegalArgumentException("Number of annihilations should be more than 2 in an OperatorBasis. Try using Basis.newBasis instead."))
        this.annihilations = annihilations
    }

    override fun create(entries: Iterable<Map.Entry<AGENT, Int>>) = OperatorBasis(creations union entries, annihilations)

    override fun create(d: AGENT, n: Int) = OperatorBasis(creations.plus(d,n), annihilations)

    override fun timesAnnihilate(d: AGENT) = OperatorBasis(creations, annihilations.plus(d,1))

    override fun forEachAnnihilationKey(keyConsumer: (AGENT) -> Unit) { annihilations.keys.forEach(keyConsumer) }

    override fun forEachAnnihilationEntry(entryConsumer: (AGENT, Int) -> Unit) { annihilations.forEach(entryConsumer) }

    override fun commuteToPerturbation(basis: CreationBasis<AGENT>, termConsumer: (Basis<AGENT>, Double) -> Unit) {
        if(annihilations.size == 1) {
            val (d, n) = annihilations.entries.first()
            val m = basis.creations[d]?:return
            commutationCoefficients(n, m).forEach { cq ->
                val newBasis = newBasis(creations.plus(d,-cq.q), mapOf(d to n - cq.q))
                termConsumer(newBasis, cq.c.toDouble())
            }
        } else {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    data class Coefficient(val c: Int, val q: Int)

    fun commutationCoefficients(n: Int, m: Int) =
            generateSequence(Coefficient(1, 0)) {
                if(it.q == n || it.q == m) return@generateSequence null
                val newq = it.q+1
                Coefficient(it.c*(n-it.q)*(m-it.q)/newq, newq)
            }.drop(1)


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