package deselby.std.collections

import deselby.std.NDRange

open class DoubleNDArray {
    protected val shapeArray    : IntArray
    protected val strideArray   : IntArray
    protected val data          : DoubleArray

    val indexSet : NDRange
        get() = NDRange(shapeArray.size) { 0 until shapeArray[it] }
    val shape : List<Int>
        get() = shapeArray.asList()
    val stride : List<Int>
        get() = strideArray.asList()
    open val size : Int
        get() = data.size
    val entries : Iterator<Pair<IntArray,Double>>
        get() = EntryIterator()

    constructor(shape : List<Int>, init : (IntArray) -> Double) {
        shapeArray = IntArray(shape.size) { shape[it] }
        strideArray = shapeToStride(shapeArray)
        data = DoubleArray(strideArray.first()*shape.first()) { init(toNDIndex(it)) }
    }

    constructor(nDimensions : Int, shapeInit : (Int) -> Int, dataInit : (IntArray) -> Double) {
        this.shapeArray = IntArray(nDimensions, shapeInit)
        this.strideArray = shapeToStride(shapeArray)
        this.data = DoubleArray(strideArray.first()*shapeArray.first()) { dataInit(toNDIndex(it)) }
    }
//    constructor(shape : IntArray, init : (IntArray) -> Double) : this(shape.asList(), init)

//    constructor(shape : NDIndexSet, init : (IntArray) -> Double) : this(shape.shape, init)

    protected constructor(copy : DoubleNDArray) : this(copy.shapeArray, copy.strideArray, copy.data)

//    protected constructor(shapeArray : IntArray, strideArray : IntArray, init : (IntArray) -> Double) {
//        this.shapeArray = indexSet
//        strideArray = stride
//        data = DoubleArray(strideArray.last()*shape.last(), {init(toNDIndex(it))})
//    }

    protected constructor(shapeArray : IntArray, data : DoubleArray) {
        this.shapeArray = shapeArray
        this.strideArray = shapeToStride(shapeArray)
        this.data = data
    }


    protected constructor(shapeArray : IntArray, strideArray : IntArray, data : DoubleArray) {
        this.shapeArray = shapeArray
        this.strideArray = strideArray
        this.data = data
    }


    fun map(mapFunc : (Double) -> Double) =
            DoubleNDArray(shapeArray, strideArray, DoubleArray(data.size) { i -> mapFunc(data[i]) })


    fun mapIndexed(mapFunc : (IntArray, Double) -> Double) =
            DoubleNDArray(shapeArray, strideArray, DoubleArray(data.size) { i -> mapFunc(toNDIndex(i), data[i]) })


    fun binaryOp(other : DoubleNDArray, mapFunc : (Double, Double) -> Double) : DoubleNDArray {
        val thisNDArray = this
        return DoubleNDArray(indexSet.rectangularUnion(other.indexSet).shape.asList()) { unionIndex ->
            mapFunc(thisNDArray.getOrNull(unionIndex) ?: 0.0, other.getOrNull(unionIndex) ?: 0.0)
        }
    }


    operator fun plus(other : DoubleNDArray) = binaryOp(other, Double::plus)

    operator fun minus(other : DoubleNDArray) = binaryOp(other, Double::minus)

    operator fun times(c : Double) = DoubleNDArray(shapeArray, strideArray, DoubleArray(data.size, { data[it] * c }))

    operator fun div(c : Double) = DoubleNDArray(shapeArray, strideArray, DoubleArray(data.size, { data[it] / c }))

    operator fun timesAssign(other : Double) {
        for(i in 0..data.lastIndex) {
            data[i] *= other
        }
    }

    operator fun divAssign(other : Double) {
        for(i in 0..data.lastIndex) {
            data[i] /= other
        }
    }

    fun dotprod(other : DoubleNDArray) : Double {
        var dp = 0.0
        if(shape != other.shape) {
            for(index in indexSet.rectangularIntersection(other.indexSet)) {
                dp += this[index]*other[index]
            }
        } else {
            for (i in 0 until size) {
                dp += data[i] * other.data[i]
            }
        }
        return dp
    }

    inline fun <R> fold(initial : R, operation : (R, Double) -> R) : R = asDoubleArray().fold(initial, operation)

    inline fun forEach(func : (Double) -> Unit) = asDoubleArray().forEach(func)

    inline fun <R> foldIndexed(initial : R, operation : (IntArray, R, Double) -> R) : R {
        var accumulator = initial
        for(entry in entries) {
            accumulator = operation(entry.first, accumulator, entry.second)
        }
        return accumulator
    }

    inline fun forEachIndexed(func : (IntArray, Double) -> Unit) {
        for(entry in entries) {
            func(entry.first, entry.second)
        }
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

    open operator fun get(slice: NDRange): DoubleNDArraySlice {
        return DoubleNDArraySlice(this, slice)
    }

    operator fun set(slice: NDRange, value: DoubleNDArray) {
        for(ndIndex in slice) {
            this[ndIndex] =value[ndIndex]
        }
    }

    operator fun set(index : IntArray, c : Double) {
        val flatI = toFlatIndex(index)
        if(flatI != null) data[flatI] = c else throw(ArrayIndexOutOfBoundsException())
    }

    inline operator fun get(x1: Int, x0 : Int) = get(intArrayOf(x1,x0))
    inline operator fun set(x1: Int, x0 : Int, v : Double) = set(intArrayOf(x1,x0),v)


    fun asDoubleArray() = data


//    private fun dimensionToStride(dim : List<Int>) : IntArray {
//        val stride = IntArray(dim.nDimensions, {1})
//        for(i in 1 until stride.nDimensions) {
//            stride[i] = stride[i-1]*dim[i-1]
//        }
//        return stride
//    }




    open fun toFlatIndex(index : IntArray) : Int? {
        var i = 0
        if(index.size != strideArray.size) return null
        for(d in 0 until index.size) {
            val id = index[d]
            if(id >= shape[d]) return null
            i += id*strideArray[d]
        }
        return i
    }

    fun toNDIndex(i : Int) : IntArray {
        var remainder = i
        // this relies on IntArray being filled in order from 0 up to nDimensions-1
        val nd = IntArray(stride.size) {
            val coordinate = remainder.div(stride[it])
            remainder = remainder.rem(stride[it])
            coordinate
        }
        return nd
    }

//    fun toNDIndex(i : Int) : IntArray {
//        val nd = IntArray(strideArray.nDimensions, {0})
//        var v = i
//        for(d in strideArray.nDimensions - 1 downTo 0) {
//            nd[d] = v.annihilate(strideArray[d])
//            v = v.rem(strideArray[d])
//        }
//        return nd
//    }

    open fun reShape(newShape : IntArray) : DoubleNDArray {
        val newStride = shapeToStride(newShape)
        if(newStride.first() * newShape.first() != size) throw(IllegalArgumentException("new shape has the wrong number of elements"))
        return DoubleNDArray(newShape, newStride, data)
    }


    protected fun shapeToStride(shape : IntArray) : IntArray {
        val newStride = IntArray(shape.size) { 1 }
        for (i in newStride.size-2 downTo 0) {
            newStride[i] = newStride[i + 1] * shape[i + 1]
        }
        return newStride
    }


    override fun toString() : String {
        var s = ""
        for(ndi in indexSet) {
            s += "${ndi.asList()} -> ${this[ndi]}\n"
        }
        return s
    }

    open operator fun iterator() : Iterator<Double> = data.iterator()

    // implements iterator with both  flat and shaped index
    // for efficient indexed iteration
    inner class EntryIterator : Iterator<Pair<IntArray,Double>> {
        var dataIterator : Iterator<Double>
        var indexIterator : Iterator<IntArray>

        constructor() {
            dataIterator = this@DoubleNDArray.iterator()
            indexIterator = this@DoubleNDArray.indexSet.iterator()
        }

        override fun hasNext() = dataIterator.hasNext()

        override fun next() : Pair<IntArray,Double> {
            return Pair(indexIterator.next(), dataIterator.next())
        }
    }

    fun asIterable() = Iterable { this.iterator() }
    fun asSequence() = Sequence { this.iterator() }
}

