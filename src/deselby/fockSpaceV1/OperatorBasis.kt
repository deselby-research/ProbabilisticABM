package deselby.fockSpaceV1

// represents a set of creation and annihilation creations in canonical order: with annihilation
// creations being applied before creation. Removal creations can be represented with -ve
// number of creations
class OperatorBasis<AGENT>(override val creations : Map<AGENT,Int>, val annihilations : Map<AGENT,Int>) : AbstractBasis<AGENT>() {

    companion object {
        fun <AGENT> identity() = OperatorBasis<AGENT>()
        fun <AGENT> create(d: AGENT) = OperatorBasis(hashMapOf(d to 1), emptyMap())
        fun <AGENT> annihilate(d: AGENT) = OperatorBasis(emptyMap(), hashMapOf(d to 1))
    }


    constructor() : this(emptyMap(), emptyMap())
    constructor(other : OperatorBasis<AGENT>) : this(HashMap(other.creations), HashMap(other.annihilations))


    override fun new(initCreations: Map<AGENT, Int>): AbstractBasis<AGENT> = OperatorBasis(initCreations, annihilations)


    // Ground state is taken to be a^annihilations
    // annihilation just adds another annihilation operator
    override fun groundStateAnnihilate(d: AGENT): MapFockState<AGENT> {
        val newAnnihilations = HashMap(annihilations)
        newAnnihilations.merge(d,1,Int::plus)
        return OperatorBasis(HashMap(), newAnnihilations).toFockState()
    }


    // multiplication with another basis means application of the creations
    override fun times(other: FockBasis<AGENT>): MapFockState<AGENT> {
        // TODO: Make this faster for case when other is OperatorBasis with multiple annihilations
        // apply creations
        var runningResult : MapFockState<AGENT> = OneHotFock(other, 1.0)
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
        return (creations == other.creations) and (annihilations == other.annihilations)
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