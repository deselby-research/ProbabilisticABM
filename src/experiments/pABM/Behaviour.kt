package experiments.pABM

import deselby.fockSpace.MapFockState

class Behaviour<AGENT>(val hamiltonian: MapFockState<AGENT>, val commutations: Map<AGENT, MapFockState<AGENT>>) {

}