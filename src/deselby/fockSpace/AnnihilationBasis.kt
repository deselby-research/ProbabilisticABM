package deselby.fockSpace

import deselby.fockSpace.extensions.LazyAnnihilationBasis
import deselby.std.abstractAlgebra.HasTimes
import deselby.std.combinatorics.combinations
import java.util.*
import kotlin.collections.HashMap

class AnnihilationBasis<AGENT>:
        OperatorSet<AGENT>,
        LazyAnnihilationBasis<AGENT>,
        HasTimes<LazyAnnihilationBasis<AGENT>,AnnihilationBasis<AGENT>> {

    companion object {
        fun <AGENT> identity() = AnnihilationBasis<AGENT>(HashMap())
        fun <AGENT> create(d: AGENT) = AnnihilationBasis(hashMapOf(d to 1))
        fun <AGENT> remove(d: AGENT) = AnnihilationBasis(hashMapOf(d to -1))
    }

    constructor(map: MutableMap<AGENT, Int> = HashMap()) : super(map)

    constructor(entries : LazyAnnihilationBasis<AGENT>) : super(entries)


    override fun iterator() = map.entries.iterator()


    fun copyOf() = AnnihilationBasis(HashMap(map))


    override operator fun times(other : LazyAnnihilationBasis<AGENT>) : AnnihilationBasis<AGENT> {
        val copy = copyOf()
        copy.unionAssign(other)
        return copy
    }


    operator fun timesAssign(other: AnnihilationBasis<AGENT>) {
        unionAssign(other)
    }


    operator fun times(ground : GroundState<AGENT>) = ground.annihilate(this)


    fun annihilate(d : AGENT, n : Int = 1) : AnnihilationBasis<AGENT> {
        val copy = copyOf()
        copy.add(d,n)
        return copy
    }


    override fun toString(): String {
        var s = ""
        for (c in map) {
            if (c.value == 1) s += "a(${c.key})" else s += "a(${c.key})^${c.value}"
        }
        return s
    }

    // using the identity a^na*^m = \sum_{q=0}^{\min(m,n)} \frac{m!n!}{q!(m-q)!(n-q)!} a*^(m-q)a^(n-q)
    operator fun times(other : CreationBasis<AGENT>) : Sequence<List<OperatorCount<AGENT>>> {
        val countSequences = ArrayDeque<CommutationSequence<AGENT>>(map.size + other.map.size)
        map.forEach {thisEntry ->
            val otherCount = other.map[thisEntry.key]?:0
            val operatorCount = CommutationSequence(thisEntry.key, thisEntry.value, otherCount)
            if(thisEntry.value > 1 || otherCount > 1)
                countSequences.addFirst(operatorCount)
            else
                countSequences.addLast(operatorCount)
        }
        other.map.forEach { otherEntry ->
            if(!map.containsKey(otherEntry.key))
                countSequences.addLast(CommutationSequence(otherEntry.key, 0, otherEntry.value))
        }
        return countSequences.combinations()
    }


    class CommutationSequence<AGENT>(val d : AGENT, var annihilations : Int, var creations : Int) :
            Sequence<OperatorCount<AGENT>> {

        override fun iterator(): Iterator<OperatorCount<AGENT>> {

            return OperatorCount(this)
        }


    }


    class OperatorCount<AGENT>(val s : CommutationSequence<AGENT>) : Iterator<OperatorCount<AGENT>> {
        private var q = -1
        private var c = 1
        private var n = s.annihilations
        private var m = s.creations

        val agent : AGENT
            get() = s.d
        val nCreations : Int
            get() = m
        val nAnnihilations : Int
            get() = n
        val coeff : Double
            get() = c.toDouble()

        override fun hasNext() = (m > 0 && n > 0)

        override fun next(): OperatorCount<AGENT> {
            if(q < 0) {
                q = 0
                return this
            }
            ++q
            --n
            --m
            c *= n*m/q
            return this
        }

    }

}