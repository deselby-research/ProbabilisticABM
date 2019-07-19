package experiments.fockBasis

import deselby.std.collections.HashMultiset

open class OperatorBasis<AGENT>(val creations : HashMultiset<AGENT>, val annihilations : HashMultiset<AGENT>) : FockBasis<AGENT> {
    override fun times(other: FockBasis<AGENT>): FockBasis<AGENT> {
        if(other !is OperatorBasis<AGENT>) throw(IllegalArgumentException("Multiplying incompatible bases"))
        return OperatorBasis(creations.union(other.creations), annihilations.union(other.annihilations))
    }

    constructor() : this(HashMultiset(), HashMultiset())

    override fun create(d: AGENT, n: Int): FockBasis<AGENT> {
        val newCreations = HashMultiset(creations)
        newCreations.add(d,n)
        return OperatorBasis(newCreations, annihilations)
    }

    // using commutation relation [a,a*^m] = ma*^(m-1)
    // a a*^m a^n = a*^m a^(n+1) + ma*^(m-1)a^n
    override fun annihilate(d: AGENT): FockState<AGENT> {
        val anplus1 = HashMultiset(annihilations)
        anplus1.add(d,1)
        val m = creations.count(d)
        if(m == 0) return OneHotFock(OperatorBasis(creations, anplus1),1.0)
        val cnminus1 =  HashMultiset(creations)
        cnminus1.remove(d,1)
        return OneHotFock(OperatorBasis(creations, anplus1), 1.0) + OneHotFock(OperatorBasis(cnminus1, annihilations),m.toDouble())
    }

    override fun count(d: AGENT): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toString() : String {
        var s = ""
        for(c in creations.memberCountMapEntries()) {
            if(c.value == 1) s += "a*(${c.key})" else s += "a*(${c.key})^${c.value}"
        }
        for(a in annihilations.memberCountMapEntries()) {
            if(a.value == 1) s += "a(${a.key})" else s += "a(${a.key})^${a.value}"
        }
        return s
    }

    override fun hashCode(): Int {
        return creations.hashCode() xor annihilations.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if(super.equals(other)) return true
        if(other !is OperatorBasis<*>) return false
        return creations == other.creations && annihilations == other.annihilations
    }

}