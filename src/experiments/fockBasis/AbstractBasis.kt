package experiments.fockBasis

abstract class AbstractBasis<AGENT>(val creations : Map<AGENT,Int>) : FockBasis<AGENT> {

    abstract fun new(creations : Map<AGENT, Int>) : AbstractBasis<AGENT>

    abstract fun groundStateAnnihilate(d: AGENT) : MapFockState<AGENT>

    override fun create(d: AGENT, n : Int): FockBasis<AGENT> {
        val delta = HashMap(creations)
        delta.merge(d, n) {a , b ->
            val newVal = a + b
            if(newVal == 0) null else newVal
        }
        return new(delta)
    }

    override fun create(creations: Map<AGENT, Int>): FockBasis<AGENT> {
        val delta = HashMap(this.creations)
        creations.forEach {
            delta.merge(it.key, it.value) {a , b ->
                val newVal = a + b
                if(newVal == 0) null else newVal
            }
        }
        return new(delta)
    }


    // using the identity
    // aa*^n = a*^(n-1)(n + a*a)
    // for n != 0
    override fun annihilate(d: AGENT): MapFockState<AGENT> {
        val nd = creations[d]?:0
        if(nd == 0) return groundStateAnnihilate(d).create(creations)
        return this.remove(d)*nd.toDouble() + groundStateAnnihilate(d).create(creations)
    }


    override fun count(d: AGENT): Int {
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
        if(super.equals(other)) return true
        if(other !is AbstractBasis<*>) return false
        return creations == other.creations
    }

}