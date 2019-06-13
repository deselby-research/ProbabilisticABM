package deselby.distributions

interface FockBasis<AGENTSTATE, BASIS : FockBasis<AGENTSTATE, BASIS>> {
    fun create(d : AGENTSTATE) : FockDecomposition<AGENTSTATE, BASIS>
    fun annihilate(d : AGENTSTATE) : FockDecomposition<AGENTSTATE, BASIS>
}