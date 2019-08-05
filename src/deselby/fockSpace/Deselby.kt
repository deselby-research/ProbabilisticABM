package deselby.fockSpace

import deselby.std.vectorSpace.DoubleVector
import deselby.std.vectorSpace.EmptyDoubleVector
import deselby.std.vectorSpace.OneHotDoubleVector

class Deselby<AGENT>(val lambda : Map<AGENT, Double>, override val creations : MutableMap<AGENT,Int> = HashMap()) :
        AbstractBasis<AGENT, Deselby<AGENT>>() {

    constructor(other: Deselby<AGENT>) : this(other.lambda, HashMap(other.creations))

    override fun new(initCreations: MutableMap<AGENT, Int>) = Deselby(lambda, initCreations)

    override fun groundStateAnnihilate(d: AGENT): DoubleVector<Deselby<AGENT>> {
        val ld = lambda[d]?:return EmptyDoubleVector()
        return OneHotDoubleVector(Deselby(lambda), ld)
    }

    fun createAssign(d : AGENT, n : Int) {
        creations.merge(d, n) { a,b ->
            val sum = a+b
            if(sum == 0) null else sum
        }
    }

}