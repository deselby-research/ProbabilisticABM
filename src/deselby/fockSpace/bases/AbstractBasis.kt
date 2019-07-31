package deselby.fockSpace.bases

import deselby.fockSpace.create
import deselby.fockSpace.times
import deselby.std.vectorSpace.DoubleVector

abstract class AbstractBasis<AGENT,BASIS : AbstractBasis<AGENT,BASIS>> : FockBasisVector<AGENT, BASIS> {
    abstract val creations : Map<AGENT,Int>

    abstract fun new(initCreations: MutableMap<AGENT, Int>) : BASIS

    abstract fun groundStateAnnihilate(d: AGENT) : DoubleVector<BASIS>

    override fun create(d: AGENT) = create(d,1)

    override fun create(d: AGENT, n : Int): BASIS {
        val delta = HashMap(creations)
        delta.merge(d, n) {a , b ->
            val newVal = a + b
            if(newVal == 0) null else newVal
        }
        return new(delta)
    }

    override fun create(newCreations: Map<AGENT, Int>): BASIS {
        val delta = HashMap(creations)
        newCreations.forEach {
            delta.merge(it.key, it.value) {a , b ->
                val newVal = a + b
                if(newVal == 0) null else newVal
            }
        }
        return new(delta)
    }


    // using the commutation relation
    // [a,a*^n] = na*^(n-1)
    // so
    // aa*^n = n.a*^(n-1) + (a*^n)a
    // for all n
    override fun annihilate(d: AGENT): DoubleVector<BASIS> {
        val nd = creations[d]?:return groundStateAnnihilate(d).create(creations)
        return this.remove(d)*nd.toDouble() + groundStateAnnihilate(d).create(creations)
    }


    fun count(d: AGENT): Int {
        return creations.getOrDefault(d,0)
    }


    override fun toString() : String {
        var s = ""
        creations.forEach {
            s += "${it.key}:${it.value} "
        }
        return s.dropLast(1)
    }


    override fun hashCode(): Int {
        return creations.hashCode()
    }


    override fun equals(other: Any?): Boolean {
//        if(super.equals(other)) return true
        if(other !is AbstractBasis<*,*>) return false
        return creations == other.creations
    }
}
