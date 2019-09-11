package deselby.distributions.continuous

import deselby.std.collections.DoubleNDArray

class SymmetricDistribution : FourierDistribution {

    constructor(nDimensions : Int, edgeSize : Int = 1, init : (IntArray) -> Double = {0.0}) : super(nDimensions, {edgeSize}, init)

    constructor(copy : DoubleNDArray) : super(copy)

    fun toLiftedSymmetric(nDimensions: Int): LiftedSymmetricDistribution {
        return LiftedSymmetricDistribution(nDimensions, this)
    }

    operator fun plus(other : SymmetricDistribution) = SymmetricDistribution(super.plus(other))

    fun degree() : Int = shape.size


}