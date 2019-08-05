package deselby.fockSpace

import deselby.fockSpace.extensions.annihilate
import deselby.fockSpace.extensions.create
import deselby.fockSpace.extensions.times
import deselby.fockSpaceV1.OperatorBasis
import deselby.std.vectorSpace.DoubleVector

class Operator<AGENT>(override val creations : Map<AGENT,Int>, val annihilations : Map<AGENT,Int>) :
        AbstractBasis<AGENT, Operator<AGENT>>()
{

    companion object {
        fun <AGENT> identity() = Operator<AGENT>()
        fun <AGENT> create(d: AGENT) = Operator(hashMapOf(d to 1), emptyMap())
        fun <AGENT> annihilate(d: AGENT) = Operator(emptyMap(), hashMapOf(d to 1))
    }


    constructor() : this(emptyMap(), emptyMap())
    constructor(other : Operator<AGENT>) : this(HashMap(other.creations), HashMap(other.annihilations))


    override fun new(initCreations: MutableMap<AGENT, Int>) = Operator(initCreations, annihilations)


    // Ground state is taken to be a^annihilations
    // annihilation just adds another annihilation operator
    override fun groundStateAnnihilate(d: AGENT): DoubleVector<Operator<AGENT>> {
        val newAnnihilations = HashMap(annihilations)
        newAnnihilations.merge(d,1,Int::plus)
        return 1.0 * Operator(HashMap(), newAnnihilations)
    }


    // multiplication with another basis means application of the operators
    operator fun<OTHERBASIS : FockBasisVector<AGENT, OTHERBASIS>> times(other: OTHERBASIS):
            DoubleVector<OTHERBASIS> {
        // TODO: Make this faster for case when other is OperatorBasis with multiple annihilations
        var runningResult : DoubleVector<OTHERBASIS> = 1.0 * other
        annihilations.forEach {
            for(i in 1..it.value) {
                runningResult = runningResult.annihilate(it.key)
            }
        }
        return runningResult.create(creations)
    }


    override fun hashCode(): Int {
        return creations.hashCode() xor annihilations.hashCode()
    }


    override fun equals(other: Any?): Boolean {
//        if(this === other) return true
        if(other !is OperatorBasis<*>) return false
        return (creations == other.creations) && (annihilations == other.annihilations)
    }


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


}