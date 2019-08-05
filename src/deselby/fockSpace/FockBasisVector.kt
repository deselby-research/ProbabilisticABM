package deselby.fockSpace

import deselby.std.vectorSpace.DoubleVector

interface FockBasisVector<AGENT, BASIS : FockBasisVector<AGENT, BASIS>> {
    fun create(d : AGENT) : BASIS
    fun create(d : AGENT, n : Int) : BASIS
    fun create(newCreations : Map<AGENT,Int>) : BASIS
    fun annihilate(d : AGENT) : DoubleVector<BASIS>

    fun remove(d: AGENT) : BASIS = create(d,-1)
}

