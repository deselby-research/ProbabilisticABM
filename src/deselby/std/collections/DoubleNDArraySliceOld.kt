package deselby.std.collections

/*
class DoubleNDArraySliceOld {
    private val strideArray    : IntArray
    private val data      : DoubleArray
    val indexSet : NDIndexSet
    val shape : List<Int>
        get() = indexSet.dimension
    val stride : List<Int>
        get() = strideArray.asList()

    val size : Int
    protected val offset : Int


    constructor(container : DoubleNDArray, sliceDirection : Int,  sliceOffset: Int) {
        strideArray = IntArray(container.stride.size-1) {d ->
            if(d<sliceDirection) container.stride[d] else container.stride[d+1]
        }
        data = container.asDoubleArray()
        indexSet = NDIndexSet(container.shape.size-1) { d ->
            if(d<sliceDirection) container.shape[d] else container.shape[d+1]
        }
        offset = container.stride[sliceDirection]*sliceOffset
        size = container.size / container.shape[sliceDirection]
    }

    private constructor(container : DoubleNDArraySliceOld, sliceDirection : Int, sliceOffset: Int, cumulativeOffset : Int) {
        strideArray = IntArray(container.stride.size-1) {d ->
            if(d<sliceDirection) container.stride[d] else container.stride[d+1]
        }
        data = container.data
        indexSet = NDIndexSet(container.shape.size-1) { d ->
            if(d<sliceDirection) container.shape[d] else container.shape[d+1]
        }
        offset = container.stride[sliceDirection]*sliceOffset + cumulativeOffset
        size = container.size / container.shape[sliceDirection]
    }

    operator fun get(index : IntArray) : Double {
        val flatI = toFlatIndex(index)
        return if(flatI != null) data[flatI] else throw(ArrayIndexOutOfBoundsException())
    }

    fun getOrNull(index : IntArray) : Double? {
        val flatI = toFlatIndex(index)
        return if(flatI != null) data[flatI] else null
    }

    fun getOrElse(index : IntArray, default : (IntArray) -> Double) : Double {
        val flatI = toFlatIndex(index)
        return if(flatI != null) data[flatI] else default(index)
    }

    operator fun set(index : IntArray, c : Double) {
        val flatI = toFlatIndex(index)
        if(flatI != null) data[flatI] = c else throw(ArrayIndexOutOfBoundsException())
    }

    fun <R> fold(initial : R, operation : (R, Double) -> R) : R =
        indexSet.fold(initial, {acc, ndIndex ->
            operation(acc, this[ndIndex])
        })


    fun toFlatIndex(index : IntArray) : Int? {
        var i = 0
        for(d in 0 until index.size) {
            val id = index[d]
            if(id >= indexSet.dimension[d]) return null
            i += id*strideArray[d]
        }
        return i + offset
    }

    override fun toString() : String {
        var s = ""
        for(ndi in indexSet) {
            s += "${ndi.asList()} -> ${this[ndi]}\n"
        }
        return s
    }

    fun toDoubleNDArray() : DoubleNDArray {
        return DoubleNDArray(shape) {get(it)}
    }


}*/
