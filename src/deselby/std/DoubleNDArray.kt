package deselby.std

import kotlin.math.max
import kotlin.math.min

open class DoubleNDArray {
    private val strideArray    : IntArray
    private val dim           : IntArray
    val dimension : List<Int>
        get() {
            return dim.asList()
        }
    val stride : List<Int>
        get() {
            return strideArray.asList()
        }
    protected val data      : DoubleArray


    constructor(dimension : List<Int>, init : (IntArray) -> Double) {
        dim = IntArray(dimension.size, {dimension[it]})
        strideArray = dimensionToStride(dim)
        data = DoubleArray(strideArray.last()*dimension.last(), {init(toNDIndex(it))})
    }


    private constructor(dimension : IntArray, stride : IntArray, init : (IntArray) -> Double) {
        dim = dimension
        strideArray = stride
        data = DoubleArray(strideArray.last()*dimension.last(), {init(toNDIndex(it))})
    }


    private constructor(dimension : IntArray, stride : IntArray, data : DoubleArray) {
        this.dim = dimension
        this.strideArray = stride
        this.data = data
    }


    fun map(mapFunc : (Double) -> Double) : DoubleNDArray {
        return DoubleNDArray(dim, strideArray, DoubleArray(data.size, { i -> mapFunc(data[i])}))
    }

    fun mapIndexed(mapFunc : (IntArray, Double) -> Double) : DoubleNDArray {
        return DoubleNDArray(dim, strideArray, DoubleArray(data.size, { i -> mapFunc(toNDIndex(i),data[i])}))
    }

    fun binaryOp(other : DoubleNDArray, mapFunc : (Double, Double) -> Double) : DoubleNDArray {
        if(dimension.size != other.dimension.size)
            throw(IllegalArgumentException("Can't perform binary operation on DoubleNDArrays of differing dimensionality"))
        val dimensionUnion = IntArray(dimension.size,{ d -> max(dimension[d], other.dimension[d]) })
        val strideUnion = dimensionToStride(dimensionUnion)
        val thisNDArray = this
        return DoubleNDArray(dimensionUnion, strideUnion, { unionIndex ->
                    mapFunc(thisNDArray[unionIndex], other[unionIndex])
                })
    }

    fun forEachIndexed(func : (IntArray, Double) -> Unit) {
        data.forEachIndexed { index, d -> func(toNDIndex(index), d) }
    }

    operator fun plus(other : DoubleNDArray) : DoubleNDArray {
        return binaryOp(other, Double::plus)
    }


    operator fun minus(other : DoubleNDArray) : DoubleNDArray {
        return binaryOp(other, Double::minus)
    }

    operator fun times(c : Double) : DoubleNDArray {
        return DoubleNDArray(dim, strideArray, DoubleArray(data.size, {data[it]*c}))
    }

    operator fun div(c : Double) : DoubleNDArray {
        return DoubleNDArray(dim, strideArray, DoubleArray(data.size, {data[it]/c}))
    }

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

    fun <R> fold(initial : R, operation : (R, Double) -> R) : R {
        return data.fold(initial, operation)
    }

    fun sum() : Double {
        return data.sum()
    }

//    operator fun get(vararg exponents : Int) : T {
//        return data[toFlatIndex(exponents)]
//    }


    operator fun get(exponents : IntArray) : Double {
        if(isOutOfBounds(exponents)) return 0.0
        return data[toFlatIndex(exponents)]
    }


    operator fun set(exponents : IntArray, c : Double) {
        if(isOutOfBounds(exponents)) throw(ArrayIndexOutOfBoundsException())
        data[toFlatIndex(exponents)] = c
    }


    fun asDoubleArray() : DoubleArray {
        return data
    }


    fun toFlatIndex(exponents : IntArray) : Int {
        var i = 0
        if(exponents.size > strideArray.size) {
            for(d in strideArray.size until exponents.size) {
                if(exponents[d] != 0) throw ArrayIndexOutOfBoundsException()
            }
        }
        for(d in 0 until min(exponents.size, strideArray.size)) {
            i += exponents[d]*strideArray[d]
        }
        return i
    }


    fun toNDIndex(i : Int) : IntArray {
        val nd = IntArray(strideArray.size, {0})
        var v = i
        for(d in strideArray.size - 1 downTo 0) {
            nd[d] = v.div(strideArray[d])
            v = v.rem(strideArray[d])
        }
        return nd
    }


    fun isOutOfBounds(exponents : IntArray) : Boolean {
        return exponents.foldIndexed(false) { index, outOfBounds, d ->
            outOfBounds || if(index < dim.size) d >= dim[index] else d > 0
        }
    }

    private fun dimensionToStride(dim : IntArray) : IntArray {
        val stride = IntArray(dim.size, {1})
        for(i in 1 until stride.size) {
            stride[i] = stride[i-1]*dim[i-1]
        }
        return stride
    }
}