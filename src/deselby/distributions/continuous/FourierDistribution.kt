package deselby.distributions.continuous

import deselby.std.DoubleNDArray
import deselby.std.NDIndexSet

open class FourierDistribution : DoubleNDArray, ContinuousDistribution {

    constructor(dimension : IntArray, init : (IntArray) -> Double) : super(dimension, init)

    protected constructor(copy : DoubleNDArray) : super(copy)

    protected constructor(indexSet : NDIndexSet, stride : IntArray, data : DoubleArray) : super(indexSet, stride, data)


    override fun lift(baseDimension : Int, nDimensions: Int): FourierDistribution {
        val newShape = NDIndexSet(this.dimension.size + nDimensions) {i ->
            val liftDimensions = baseDimension until baseDimension + nDimensions
            when {
                i < baseDimension -> dimension[i]
                i in liftDimensions -> 1
                else -> dimension[i-nDimensions]
            }
        }
        return FourierDistribution(newShape, newShape.toStride(), asDoubleArray())
    }


    override fun lower(baseDimension: Int, nDimensions: Int): FourierDistribution {
        for(d in baseDimension until baseDimension + nDimensions) {
            if(dimension[d] != 1) throw(ArrayIndexOutOfBoundsException("Trying to lower a dimension that isn't constant"))
        }
        val newShape = NDIndexSet(this.dimension.size - nDimensions) {i ->
            if(i < baseDimension) dimension[i] else dimension[i+nDimensions]
        }
        return FourierDistribution(newShape, newShape.toStride(), asDoubleArray())
    }

    operator fun plus(other : FourierDistribution) = FourierDistribution(super.plus(other))

}
