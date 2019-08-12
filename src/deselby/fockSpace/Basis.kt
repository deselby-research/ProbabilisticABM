package deselby.fockSpace

import deselby.std.vectorSpace.OneHotDoubleVector

abstract class Basis<AGENT>(val creations : Map<AGENT,Int>) {

    abstract fun multiplyTo(otherBasis: CreationBasis<AGENT>,
                            ground: GroundState<AGENT>,
                            termConsumer: (CreationBasis<AGENT>, Double) -> Unit)
    abstract fun multiplyTo(groundBasis: GroundBasis<AGENT>,
                            termConsumer: (CreationBasis<AGENT>, Double) -> Unit)
    abstract fun create(d: AGENT, n: Int=1): Basis<AGENT>
    abstract fun annihilate(d: AGENT): Basis<AGENT>
    abstract fun commute(basis: CreationBasis<AGENT>, termConsumer:(Basis<AGENT>, Double) -> Unit)
    abstract fun forEachAnnihilationKey(keyConsumer: (AGENT) -> Unit)

//    abstract fun commutationsTo(termConsumer:(AGENT, Basis<AGENT>, Double) -> Unit)

    fun toVector(weight: Double = 1.0) = OneHotDoubleVector(this, weight)

    override fun toString(): String {
        var s = ""
        for (c in creations) {
            if (c.value == 1) s += "a*(${c.key})" else s += "a*(${c.key})^${c.value}"
        }
        return s
    }


    companion object {
        fun<AGENT> identity() = CreationBasis<AGENT>(emptyMap())

        fun<AGENT> identityVector() = identity<AGENT>().toVector()

        fun<AGENT> identityCreationVector() = identity<AGENT>().toCreationVector()

        inline infix fun<AGENT> Map<AGENT, Int>.union(other: Map<AGENT, Int>): Map<AGENT, Int> {
            val union = HashMap<AGENT,Int>(this)
            union.unionAssign(other)
            return union
        }


        inline fun<AGENT> Map<AGENT, Int>.union(vararg entries: Pair<AGENT,Int>): Map<AGENT, Int> {
            val union = HashMap(this)
            entries.forEach { union.plusAssign(it.first, it.second) }
            return union
        }


        inline fun<AGENT> Map<AGENT, Int>.plus(d: AGENT, n: Int): Map<AGENT, Int> {
            val union = HashMap(this)
            union.plusAssign(d,n)
            return union
        }

        inline fun<AGENT> MutableMap<AGENT, Int>.unionAssign(other: Map<AGENT, Int>) =
            other.forEach { plusAssign(it.key, it.value) }


        inline fun<AGENT> MutableMap<AGENT, Int>.plusAssign(d: AGENT, n: Int) {
            merge(d, n) { a, b ->
                val sum = a + b
                if (sum == 0) null else sum
            }
        }

    }
}