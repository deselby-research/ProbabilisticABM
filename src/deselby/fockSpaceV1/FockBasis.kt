package deselby.fockSpaceV1

// A Fock basis consists of a base state and a creation operator
// The set of basis states are those that can be expressed as
// any number of creation creations on the base state.
//
// The annihilation operator transforms a basis state into
// a weighted sum of basis states.
interface FockBasis<AGENT> : Fockable<AGENT> {
    override fun annihilate(d : AGENT) : MapFockState<AGENT>
    override fun create(d : AGENT, n : Int) : FockBasis<AGENT>
    override fun create(d : AGENT) : FockBasis<AGENT> = create(d,1)
    override fun create(newCreations: Map<AGENT, Int>): FockBasis<AGENT>

    fun count(d : AGENT) : Int


    fun number(d : AGENT) = this.annihilate(d).create(d)

    fun remove(d : AGENT) : FockBasis<AGENT> = create(d,-1)

    operator fun times(multiplier : Double) : OneHotFock<AGENT> {
        return OneHotFock(this, multiplier)
    }

    operator fun times(other : FockBasis<AGENT>) : MapFockState<AGENT>

    fun toFockState() : MapFockState<AGENT> = OneHotFock(this)

}
