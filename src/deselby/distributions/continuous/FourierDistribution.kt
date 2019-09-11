package deselby.distributions.continuous

import deselby.std.collections.DoubleNDArray
import org.apache.commons.math3.transform.*

open class FourierDistribution : DoubleNDArray {

    constructor(shape : List<Int>, init : (IntArray) -> Double) : super(shape, init)

    constructor(nDimensions : Int, shapeInit : (Int) -> Int, dataInit : (IntArray) -> Double) : super(nDimensions, shapeInit, dataInit)

    protected constructor(copy : DoubleNDArray) : super(copy)
    protected constructor(shape : IntArray, data : DoubleArray) : super(shape, data)
    protected constructor(shape : IntArray, stride : IntArray, data : DoubleArray) : super(shape, stride, data)

    // assumes integration over all dimensions of other with nDimensions greater than one
//    fun integralOfProduct(other: FourierDistribution, dimensionsToIntegrate : BooleanArray): FourierDistribution {
//        val resultShape = IntArray(shape.size) {
//            if(dimensionsToIntegrate[it]) 1 else this.shape[it] + other.shape[it] - 1
//        }
//    }

    fun fft(dimensionsToTransform : BooleanArray, forwardInverse : TransformType) {
        var myFFT = FastCosineTransformer(DctNormalization.STANDARD_DCT_I)
        myFFT.transform(doubleArrayOf(1.0,2.0),TransformType.FORWARD)

    }



    fun lift(baseDimension : Int, nDimensions: Int): FourierDistribution {
        val newShape = IntArray(this.shape.size + nDimensions) { i ->
            val liftDimensions = baseDimension until baseDimension + nDimensions
            when {
                i < baseDimension -> shape[i]
                i in liftDimensions -> 1
                else -> shape[i-nDimensions]
            }
        }
        return FourierDistribution(newShape, data)
    }


    fun lower(baseDimension: Int, nDimensions: Int): FourierDistribution {
        for(d in baseDimension until baseDimension + nDimensions) {
            if(shape[d] != 1) throw(ArrayIndexOutOfBoundsException("Trying to lower a shape that isn't constant"))
        }
        val newShape = IntArray(this.shape.size - nDimensions) { i ->
            if(i < baseDimension) shape[i] else shape[i+nDimensions]
        }
        return FourierDistribution(newShape, data)
    }

    operator fun plus(other : FourierDistribution) = FourierDistribution(super.plus(other))

}
