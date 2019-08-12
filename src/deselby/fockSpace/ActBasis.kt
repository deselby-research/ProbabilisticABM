package deselby.fockSpace

import deselby.std.vectorSpace.MutableDoubleVector
import deselby.std.vectorSpace.OneHotDoubleVector

abstract class ActBasis<AGENT>(val creations : Map<AGENT,Int>) {

    abstract fun multiplyTo(otherBasis: ActCreationBasis<AGENT>,
                            ground: GroundState<AGENT>,
                            termConsumer: (ActCreationBasis<AGENT>, Double) -> Unit)

    abstract fun multiplyTo(groundBasis: GroundBasis<AGENT>,
                            termConsumer: (ActCreationBasis<AGENT>, Double) -> Unit)


    fun create(d: AGENT) = create(d,1)

    abstract fun create(d: AGENT, n: Int): ActBasis<AGENT>

    abstract fun annihilate(d: AGENT): ActBasis<AGENT>

    fun toVector() = OneHotDoubleVector(this, 1.0)

    override fun toString(): String {
        var s = ""
        for (c in creations) {
            if (c.value == 1) s += "a*(${c.key})" else s += "a*(${c.key})^${c.value}"
        }
        return s
    }


    companion object {
        fun<AGENT> identity() = ActCreationBasis<AGENT>(emptyMap())

        inline infix fun<AGENT> Map<AGENT, Int>.union(other: Map<AGENT, Int>): Map<AGENT, Int> {
//            if (isEmpty()) return other
//            if (other.isEmpty()) return this
            val union = HashMap<AGENT,Int>(this)
            other.forEach {
                union.merge(it.key, it.value) { a, b ->
                    val sum = a + b
                    if (sum == 0) null else sum
                }
            }
            return union
        }


        inline fun<AGENT> Map<AGENT, Int>.plus(d: AGENT, n: Int): Map<AGENT, Int> {
            // if (this.isEmpty()) return mapOf(d to n)
            val union = HashMap(this)
            union.merge(d, n) { a, b ->
                val sum = a + b
                if (sum == 0) null else sum
            }
            return union
        }

        inline fun<AGENT> Map<AGENT, Int>.union(vararg entries: Pair<AGENT,Int>): Map<AGENT, Int> {
            // if (this.isEmpty()) return mapOf(d to n)
            val union = HashMap(this)
            entries.forEach {
                union.merge(it.first, it.second) { a, b ->
                    val sum = a + b
                    if (sum == 0) null else sum
                }
            }
            return union
        }


    }
}