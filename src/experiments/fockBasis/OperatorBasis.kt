package experiments.fockBasis

import deselby.std.collections.HashMultiset

open class OperatorBasis<AGENT>(creations : Map<AGENT,Int>, val annihilations : HashMap<AGENT,Int>) : AbstractBasis<AGENT>(creations) {
    override fun new(creations: Map<AGENT, Int>): AbstractBasis<AGENT> = OperatorBasis(creations, annihilations)

    // Ground state is taken to be a^annihilations
    // annihilation just adds another annihilation operator
    override fun groundStateAnnihilate(d: AGENT): MapFockState<AGENT> {
        return OneHotFock(OperatorBasis(HashMap(), hashMapOf(d to annihilations.getOrDefault(d,0)+1)),1.0)
    }

    // multiplication with another basis means application of the operators
    override fun times(other: FockBasis<AGENT>): MapFockState<AGENT> {
        if(other is OperatorBasis<AGENT>) {
            val newCreations = HashMap(creations)
            other.creations.forEach {
                newCreations.merge(it.key, it.value) {a , b ->
                    val newVal = a + b
                    if(newVal == 0) null else newVal
                }
            }
            val newAnnihilations = HashMap(annihilations)
            other.creations.forEach {
                newAnnihilations.merge(it.key, it.value,Int::plus)
            }
            return OneHotFock(OperatorBasis(newCreations, newAnnihilations),1.0)
        }
        // apply operators
        var runningResult : MapFockState<AGENT> = OneHotFock(other,1.0)
        annihilations.forEach {
            for(i in 1..it.value) {
                runningResult = runningResult.annihilate(it.key)
            }
        }
        return runningResult.create(creations)
    }

    constructor() : this(HashMap(), HashMap())


    // using commutation relation [a,a*^m] = ma*^(m-1)
    // a a*^m a^n = a*^m a^(n+1) + ma*^(m-1)a^n
    // = a*^(m-1)(m + a*a)a^n
//    override fun annihilate(d: AGENT): MapFockState<AGENT> {
//        val anplus1 = HashMultiset(annihilations)
//        anplus1.add(d,1)
//        val m = creations[d]?:return OneHotFock(OperatorBasis(creations, anplus1),1.0)
//        val cnminus1 =  HashMap(creations)
//        cnminus1.compute(d) {_,currentVal ->
//            val newVal = (currentVal?:0)-1
//            if(newVal == 0) null else newVal
//        }
//        return OneHotFock(OperatorBasis(creations, anplus1), 1.0) + OneHotFock(OperatorBasis(cnminus1, annihilations),m.toDouble())
//    }

    override fun toString() : String {
        var s = ""
        for(c in creations) {
            if(c.value == 1) s += "a*(${c.key})" else s += "a*(${c.key})^${c.value}"
        }
        for(a in annihilations) {
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