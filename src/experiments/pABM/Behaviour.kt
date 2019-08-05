package experiments.pABM

import deselby.fockSpaceV1.MapFockState

class Behaviour<AGENT>(val hamiltonian: MapFockState<AGENT>, val commutations: Map<AGENT, MapFockState<AGENT>>) {

}