package experiments.fockBasis

import deselby.std.abstractAlgebra.AlgebraElement

interface FockState<AGENT, STATE : FockState<AGENT,STATE>> : Fockable<AGENT>, AlgebraElement<STATE, Double> {

    override fun create(a : AGENT, n : Int) : STATE
    override fun create(a : AGENT) : STATE = create(a,1)
    override fun create(creations: Map<AGENT, Int>): STATE
    override fun annihilate(a : AGENT) : STATE

    override operator fun unaryMinus(): STATE {
        return(this * (-1.0))
    }
}

