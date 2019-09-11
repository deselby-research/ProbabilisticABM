package deselby.distributions.continuous

import deselby.std.collections.DoubleNDArray
import deselby.std.NDRange
import kotlin.math.max

class LiftedSymmetricDistribution : FourierDistribution, SemiSymmetricDistribution {
    val nLifted : Int

    constructor(nLifted : Int, base : SymmetricDistribution) : super((base as FourierDistribution).lift(0, nLifted)) {
        this.nLifted = nLifted
    }

    constructor(nLifted : Int, shape : List<Int>, init : (IntArray) -> Double) : super(shape, init) {
        this.nLifted = nLifted
    }

    private constructor(nLifted : Int, base : DoubleNDArray) : super(base) {
        this.nLifted = nLifted
    }

    override fun nameDimension(name : Int) : LiftedSymmetricDistribution {
        if(shapeArray[name] != 1) throw(ArrayIndexOutOfBoundsException("That name does not exist or has already been used"))
        val newShape = IntArray(shapeArray.size-1) {i ->
            if(i == name) shapeArray.last() else shapeArray[i]
        }
        return LiftedSymmetricDistribution(nLifted, reShape(newShape))
    }

    // to integrate along a shape, just take zeroth element
    override fun integrate(dimensions: BooleanArray) : LiftedSymmetricDistribution {
        val slice = NDRange(shapeArray.size) { if(dimensions[it]) 0..0 else 0 until shapeArray[it] }
        val dimensionsNotIntegrated = dimensions
                .asSequence()
                .mapIndexed{ i, toIntegrate -> if(toIntegrate) null else i}
                .filterNotNull()
                .toList()
        val newShape = IntArray(dimensionsNotIntegrated.size) { shapeArray[dimensionsNotIntegrated[it]] }
        return LiftedSymmetricDistribution(nLifted, this[slice].reShape(*newShape))
    }

    override fun lower(nDimensions: Int) : SymmetricDistribution {
        return SymmetricDistribution(super.lower(0, nDimensions))
    }

    fun symmetrise(parameterId : Int) : LiftedSymmetricDistribution {
        val newSymmetricSize = if(nLifted == shape.size) shape[parameterId] else max(shape[parameterId], shape.last())
        val newShape = IntArray(shape.size + 1) { i ->
            when { // swap parameterId shape to last shape
                i==parameterId -> 1
                i<nLifted -> shape[i]
                else -> newSymmetricSize
            }
        }
        return LiftedSymmetricDistribution(nLifted, newShape.asList()) { ndi ->
            var sum = 0.0
            for(transposedDimension in nLifted..newShape.size) {
                val transposedIndex = IntArray(shape.size) { i ->
                    if(i == parameterId) ndi[transposedDimension] else if(i < transposedDimension) ndi[i] else ndi[i+1]
                }
                sum += get(transposedIndex)
            }
            sum
        }
    }

}