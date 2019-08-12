package deselby.fockSpace

import deselby.fockSpace.extensions.LazyAnnihilationBasis
import deselby.std.vectorSpace.DoubleVector
import deselby.std.vectorSpace.OneHotDoubleVector

class GeneralisedGroundState<AGENT>(val creations : MutableMap<AGENT,Int>, val trueGround : GroundState<AGENT>) : GroundState<AGENT> {
    override fun lambda(d: AGENT): Double {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    // aa*^m = a*^ma + [a,a*^m] = a*^ma + ma^-a*^m
    // if A is a0 then aa*^m = (A + ma^-)a*^m
    override fun annihilate(d: AGENT): DoubleVector<CreationBasis<AGENT>> {
        val m = creations[d]?:return trueGround.annihilate(d)
        return trueGround.annihilate(d) + OneHotDoubleVector(CreationBasis.remove(d), m.toDouble())
    }

    override fun annihilate(annihilations: LazyAnnihilationBasis<AGENT>): DoubleVector<CreationBasis<AGENT>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    operator fun timesAssign(basis : CreationBasis<AGENT>) {
        TODO()
    }
}