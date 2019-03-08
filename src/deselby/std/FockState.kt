package deselby.std

interface FockState<AGENTSTATE> {
    fun create(agent : AGENTSTATE) : FockState<AGENTSTATE>
    fun annihilate(agent : AGENTSTATE) : FockState<AGENTSTATE>
    operator fun plus(other : FockState<AGENTSTATE>) : FockState<AGENTSTATE>
    operator fun minus(other : FockState<AGENTSTATE>) : FockState<AGENTSTATE>
}