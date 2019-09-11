package deselby.fockSpaceV1

import deselby.std.abstractAlgebra.AlgebraElement

interface FockState<AGENT, STATE : FockState<AGENT, STATE>> : Fockable<AGENT>, AlgebraElement<STATE, Double> {

    override fun create(d : AGENT, n : Int) : STATE
    override fun create(d : AGENT) : STATE = create(d,1)
    override fun create(creations: Map<AGENT, Int>): STATE
    override fun annihilate(d : AGENT) : STATE

    override operator fun unaryMinus(): STATE {
        return(this * (-1.0))
    }
}

