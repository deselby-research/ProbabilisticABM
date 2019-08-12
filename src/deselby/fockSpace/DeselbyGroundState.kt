package deselby.fockSpace

import deselby.fockSpace.extensions.LazyAnnihilationBasis
import deselby.std.vectorSpace.DoubleVector
import deselby.std.vectorSpace.OneHotDoubleVector
import kotlin.math.pow

class DeselbyGroundState<AGENT>(val lambdas : Map<AGENT,Double>) : GroundState<AGENT> {

    constructor(vararg lambdas: Pair<AGENT,Double>) :this(hashMapOf(*lambdas))

    override fun annihilate(d: AGENT): DoubleVector<CreationBasis<AGENT>> {
        return OneHotDoubleVector(CreationBasis.identity(),lambdas[d]?:0.0)
    }


//    override fun annihilate(annihilations: Map<AGENT, Int>): DoubleVector<CreationBasis<AGENT>> {
//        var multiplier = 1.0
//        annihilations.forEach {
//            multiplier *= lambdas[it.key]?.pow(it.value)?:0.0
//        }
//        return OneHotDoubleVector(CreationBasis.identity(), multiplier)
//    }

    override fun annihilate(annihilations: LazyAnnihilationBasis<AGENT>): DoubleVector<CreationBasis<AGENT>> {
        var multiplier = 1.0
        annihilations.forEach {
            multiplier *= lambdas[it.key]?.pow(it.value)?:0.0
        }
        return OneHotDoubleVector(CreationBasis.identity(), multiplier)
    }

    override fun lambda(d : AGENT) = this.lambdas[d]?:0.0

}