package deselby.distributions

interface FockState<AGENTSTATE, FOCKSTATE : FockState<AGENTSTATE,FOCKSTATE> > {
    fun create(d : AGENTSTATE) : FOCKSTATE
    fun annihilate(d : AGENTSTATE) : FOCKSTATE
    fun number(d : AGENTSTATE) : FOCKSTATE = this.annihilate(d).create(d)
//    fun sample() : FOCKSTATE
    operator fun plus(other : FOCKSTATE) : FOCKSTATE
    operator fun minus(other : FOCKSTATE) : FOCKSTATE
    operator fun times(const : Double) : FOCKSTATE
}