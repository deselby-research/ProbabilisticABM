package deselby.std.collections

import deselby.std.NDRange
import deselby.std.NDRangeIterator
import java.lang.IllegalArgumentException

class DoubleNDArraySlice : DoubleNDArray {
    var offset : Int

    override val size : Int
        get() = shape.fold(1, Int::times)

    constructor(base : DoubleNDArraySlice, slice : NDRange) : this(base as DoubleNDArray, slice) {
        offset += base.offset
    }


    constructor(base : DoubleNDArray, slice : NDRange) : super(
            slice.shape,
            IntArray(slice.nDimensions) {
                base.stride[it]*slice[it].step
            },
            base.asDoubleArray()
    ) {
        offset = base.toFlatIndex(slice.first()) ?:
                throw(ArrayIndexOutOfBoundsException("Slice contains out of bounds elements"))
//        println("Creating slice with ${slice.first().asList()} to ${slice.last().asList()}")
//        println("offset is $offset")
        for(i in 0 until slice.nDimensions) {
            if(slice[i].last >= base.shape[i])
                throw(ArrayIndexOutOfBoundsException("Slice contains out of bounds elements"))
        }
    }

    constructor(shapeArray : IntArray, strideArray : IntArray, data : DoubleArray, offset : Int) : super(shapeArray, strideArray, data) {
        this.offset = offset
    }

    override fun toFlatIndex(index : IntArray) : Int? {
        return super.toFlatIndex(index)?.plus(offset)
    }

    override fun get(slice: NDRange): DoubleNDArraySlice {
        return DoubleNDArraySlice(this, slice)
    }


    override fun reShape(vararg newShape : Int) : DoubleNDArraySlice {
        val newStride = IntArray(newShape.size) {1}
        var oldDim = shapeArray.size-1
        var dim = newShape.size-1
        while(dim >= 0) {
            when {
                newShape[dim] == shape[oldDim] -> newStride[dim--] = stride[oldDim--]
                newShape[dim] > shape[oldDim]  ->
                    if(shape[oldDim] == 1) --oldDim
                    else throw(IllegalArgumentException("Can't reshape a slice in this way"))
                newShape[dim] < shape[oldDim]  -> {
                    var stride = stride[oldDim]
                    var size = 1
                    do {
                        newStride[dim] = stride
                        size *= newShape[dim]
                        stride *= newShape[dim--]
                    } while(size < shape[oldDim] && dim >= 0)
                    if(size > shape[oldDim]) throw(IllegalArgumentException("Can't reshape a slice in this way"))
                    --oldDim
                }
            }
        }
        return DoubleNDArraySlice(newShape, newStride, data, offset)
    }

    override operator fun iterator() = DataIterator()

    inner class DataIterator : Iterator<Double> {
        val indexIterator = NDRangeIterator(this@DoubleNDArraySlice.indexSet.range)
        override fun hasNext() = indexIterator.hasNext()
        override fun next() = this@DoubleNDArraySlice[indexIterator.next()]
    }

}