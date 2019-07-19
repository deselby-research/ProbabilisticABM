package experiments.fockBasis

import deselby.std.abstractAlgebra.AlgebraElement

interface FockState<AGENT, STATE : FockState<AGENT,STATE>> : Fockable<AGENT>, AlgebraElement<STATE, Double> {

    override fun create(a : AGENT, n : Int) : STATE
    override fun create(a : AGENT) : STATE
    override fun annihilate(a : AGENT) : STATE



}
