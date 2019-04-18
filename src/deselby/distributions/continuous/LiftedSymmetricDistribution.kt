package deselby.distributions.continuous

import deselby.std.DoubleNDArray
import deselby.std.DoubleNDArraySlice
import deselby.std.NDIndexSet
import kotlin.math.max

class LiftedSymmetricDistribution : FourierDistribution, SemiSymmetricDistribution {
    val nLifted : Int

    constructor(nLifted : Int, base : SymmetricDistribution) : super((base as FourierDistribution).lift(0, nLifted)) {
        this.nLifted = nLifted
    }

    constructor(nLifted : Int, dimension : IntArray, init : (IntArray) -> Double) : super(dimension, init) {
        this.nLifted = nLifted
    }

    private constructor(nLifted : Int, base : DoubleNDArray) : super(base) {
        this.nLifted = nLifted
    }

    override fun nameDimension(name : Int) : LiftedSymmetricDistribution {
        if(indexSet.dimension[name] != 1) throw(ArrayIndexOutOfBoundsException("That name does not exist or has already been used"))
        val newShape = NDIndexSet(indexSet.dimension.size-1) {i ->
            if(i == name) indexSet.dimension.last() else indexSet.dimension[i]
        }
        return LiftedSymmetricDistribution(nLifted, reinterpretShape(newShape))
    }

    override fun integrate(dimensions: BooleanArray) : LiftedSymmetricDistribution {
        var integratedDist : DoubleNDArraySlice? = null
        for(dim in 0 until dimensions.size) {
            if(dimensions[dim]) integratedDist = integratedDist?.slice(dim, 0)?:slice(dim, 0)
        }
        return if(integratedDist != null) {
            LiftedSymmetricDistribution(nLifted, integratedDist.toDoubleNDArray())
        } else {
            this
        }
    }

    override fun lower(nDimensions: Int) : SymmetricDistribution {
        return SymmetricDistribution(super.lower(0, nDimensions))
    }

    fun symmetrise(parameterId : Int) : LiftedSymmetricDistribution {
        val newSymmetricSize = if(nLifted == dimension.size) dimension[parameterId] else max(dimension[parameterId], dimension.last())
        val newDimemsion = IntArray(dimension.size + 1) {i ->
            when { // swap parameterId dimension to last dimension
                i==parameterId -> 1
                i<nLifted -> dimension[i]
                else -> newSymmetricSize
            }
        }
        return LiftedSymmetricDistribution(nLifted, newDimemsion) {ndi ->
            var sum = 0.0
            for(transposedDimension in nLifted..newDimemsion.size) {
                val transposedIndex = IntArray(dimension.size) {i ->
                    if(i == parameterId) ndi[transposedDimension] else if(i < transposedDimension) ndi[i] else ndi[i+1]
                }
                sum += get(transposedIndex)
            }
            sum
        }
    }

}