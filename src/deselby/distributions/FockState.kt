package deselby.distributions

interface FockState<AGENTSTATE, DISTRIBUTION : FockState<AGENTSTATE,DISTRIBUTION> > {
    fun create(agent : AGENTSTATE) : DISTRIBUTION
    fun annihilate(agent : AGENTSTATE) : DISTRIBUTION
    operator fun plus(other : DISTRIBUTION) : DISTRIBUTION
    operator fun minus(other : DISTRIBUTION) : DISTRIBUTION
    operator fun times(other : Double) : DISTRIBUTION
}