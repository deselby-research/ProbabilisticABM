package deselby.fockSpace

// represents a set of creation and annihilation operators in canonical order: with annihilation
// operators being applied before creation. Removal operators can be represented with -ve
// number of creations
class OperatorBasis<AGENT>(creations : Map<AGENT,Int>, val annihilations : HashMap<AGENT,Int>) : AbstractBasis<AGENT>(creations) {

    companion object {
        fun <AGENT> identity() = OperatorBasis<AGENT>()
        fun <AGENT> create(d: AGENT) = OperatorBasis(hashMapOf(d to 1), HashMap())
        fun <AGENT> annihilate(d: AGENT) = OperatorBasis(HashMap(), hashMapOf(d to 1))
    }


    constructor() : this(HashMap(), HashMap())


    override fun new(creations: Map<AGENT, Int>): AbstractBasis<AGENT> = OperatorBasis(creations, annihilations)


    // Ground state is taken to be a^annihilations
    // annihilation just adds another annihilation operator
    override fun groundStateAnnihilate(d: AGENT): MapFockState<AGENT> {
        return OneHotFock(OperatorBasis(HashMap(), hashMapOf(d to annihilations.getOrDefault(d,0)+1)),1.0)
    }


    // multiplication with another basis means application of the operators
    override fun times(other: FockBasis<AGENT>): MapFockState<AGENT> {
        // TODO: Make this faster for case when other is OperatorBasis with multiple annihilations
        // apply operators
        var runningResult : MapFockState<AGENT> = OneHotFock(other,1.0)
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
        if(super.equals(other)) return true
        if(other !is OperatorBasis<*>) return false
        return creations == other.creations && annihilations == other.annihilations
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