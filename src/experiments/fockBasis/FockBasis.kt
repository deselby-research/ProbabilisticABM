package experiments.fockBasis

// A Fock basis consists of a base state and a creation operator
// The set of basis states are those that can be expressed as
// any number of creation operators on the base state.
//
// The annihilation operator transforms a basis state into
// a weighted sum of basis states.
interface FockBasis<AGENT> : Fockable<AGENT> {
    override fun create(d : AGENT, n : Int) : FockBasis<AGENT>
    override fun annihilate(d : AGENT) : AbstractFockState<AGENT>
    fun count(d : AGENT) : Int

    override fun create(d : AGENT) : FockBasis<AGENT> = create(d,1)

    fun number(d : AGENT) : AbstractFockState<AGENT> = this.annihilate(d).create(d)

    fun remove(d : AGENT) : FockBasis<AGENT> = create(d,-1)

    operator fun times(multiplier : Double) : OneHotFock<AGENT> {
        return OneHotFock(this, multiplier)
    }

    operator fun times(other : FockBasis<AGENT>) : FockBasis<AGENT>
}
