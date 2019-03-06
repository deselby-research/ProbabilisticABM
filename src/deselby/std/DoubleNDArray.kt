package deselby.std

import kotlin.math.max

open class DoubleNDArray {
    private val strideArray    : IntArray
    private val dim           : IntArray
    protected val data      : DoubleArray
    val dimension : List<Int>
        get() = dim.asList()

    val stride : List<Int>
        get() = strideArray.asList()
    val size : Int
        get() = data.size
    val indexSet = IndexSet()


    inner class IndexSet : Set<Index> {
        override val size: Int
            get() = this@DoubleNDArray.size

        override fun contains(element: Index): Boolean {
            return element.getNDArray() == this@DoubleNDArray && !element.isOutOfBounds()
        }

        override fun containsAll(elements: Collection<Index>): Boolean {
            return elements.fold(true, {acc, index -> acc && contains(index)})
        }

        override fun isEmpty(): Boolean {
            return dim.isEmpty() || dim.fold(false, {acc, i -> acc || i == 0})
        }

        override fun iterator(): Iterator<Index> {
            return Index()
        }
    }

    inner class Index(private var index : IntArray) : Iterator<Index> {

        val lastIndex : Int
            get() = index.lastIndex
        val size : Int
            get() = index.size

        constructor() : this(IntArray(dim.size,{i -> if(i==0) -1 else 0}))

        constructor(flatIndex : Int) : this(IntArray(strideArray.size, {0})) {
            var v = flatIndex
            for(d in strideArray.size - 1 downTo 0) {
                index[d] = v.div(strideArray[d])
                v = v.rem(strideArray[d])
            }
        }

        constructor(otherIndex : Index) : this(IntArray(strideArray.size, { d ->
            if(otherIndex[d] >= dim[d]) throw(ArrayIndexOutOfBoundsException())
            if(d < otherIndex.size) otherIndex[d] else 0
        })) {
             for(d in size until otherIndex.size) {
                 if(otherIndex[d] != 0) throw(ArrayIndexOutOfBoundsException())
             }
        }

        operator fun get(d : Int) : Int {
            return index[d]
        }

        operator fun set(d : Int, i : Int) {
            index[d] = i
        }

        override fun hasNext(): Boolean {
            return index.foldIndexed(false, {dimension, acc, i -> acc || (i < dim[dimension]-1)})
        }

        override fun next(): Index {
            var i = 0
            while(++index[i] == dim[i]) {
                index[i++] = 0
            }
            return this
        }

        fun toFlatIndex() : Int {
            var i = 0
            for(d in 0 until index.size) {
                val id = index[d]
                if(id >= dim[d]) throw(ArrayIndexOutOfBoundsException())
                i += id*strideArray[d]
            }
            return i
        }

        fun isOutOfBounds() : Boolean {
            return index.foldIndexed(false) { dimension, outOfBounds, d ->
                outOfBounds || d >= dim[dimension]
            }
        }

        fun getNDArray() : DoubleNDArray {
            return this@DoubleNDArray
        }

        fun copyOf() : Index {
            return Index(index.copyOf())
        }

        override fun toString() : String {
            return index.asList().toString()
        }

    }


    constructor(dimension : List<Int>, init : (Index) -> Double) {
        dim = IntArray(dimension.size, {dimension[it]})
        strideArray = dimensionToStride(dim)
        data = DoubleArray(strideArray.last()*dimension.last(), {init(Index(it))})
    }


    private constructor(dimension : IntArray, stride : IntArray, init : (Index) -> Double) {
        dim = dimension
        strideArray = stride
        data = DoubleArray(strideArray.last()*dimension.last(), {init(Index(it))})
    }


    private constructor(dimension : IntArray, stride : IntArray, data : DoubleArray) {
        this.dim = dimension
        this.strideArray = stride
        this.data = data
    }


    fun map(mapFunc : (Double) -> Double) : DoubleNDArray {
        return DoubleNDArray(dim, strideArray, DoubleArray(data.size, { i -> mapFunc(data[i])}))
    }

    fun mapIndexed(mapFunc : (Index, Double) -> Double) : DoubleNDArray {
        return DoubleNDArray(dim, strideArray, DoubleArray(data.size, { i -> mapFunc(Index(i),data[i])}))
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

    operator fun get(index : Index) : Double {
        return data[index.toFlatIndex()]
    }



    operator fun set(index : Index, c : Double) {
        data[index.toFlatIndex()] = c
    }


    fun asDoubleArray() : DoubleArray {
        return data
    }


    private fun dimensionToStride(dim : IntArray) : IntArray {
        val stride = IntArray(dim.size, {1})
        for(i in 1 until stride.size) {
            stride[i] = stride[i-1]*dim[i-1]
        }
        return stride
    }


    fun forEachIndexed(func : (Index, Double) -> Unit) {
        data.forEachIndexed { flatIndex, d -> func(Index(flatIndex), d) }
    }


//    fun toNDIndex(i : Int) : IntArray {
//        val nd = IntArray(strideArray.size, {0})
//        var v = i
//        for(d in strideArray.size - 1 downTo 0) {
//            nd[d] = v.div(strideArray[d])
//            v = v.rem(strideArray[d])
//        }
//        return nd
//    }


//    operator fun get(vararg exponents : Int) : T {
//        return data[toFlatIndex(exponents)]
//    }


//    operator fun get(exponents : IntArray) : Double {
//        if(isOutOfBounds(exponents)) return 0.0
//        return data[toFlatIndex(exponents)]
//    }

//    operator fun set(exponents : IntArray, c : Double) {
//        if(isOutOfBounds(exponents)) throw(ArrayIndexOutOfBoundsException())
//        data[toFlatIndex(exponents)] = c
//    }
//    fun toFlatIndex(exponents : IntArray) : Int {
//        var i = 0
//        if(exponents.size > strideArray.size) {
//            for(d in strideArray.size until exponents.size) {
//                if(exponents[d] != 0) throw ArrayIndexOutOfBoundsException()
//            }
//        }
//        for(d in 0 until min(exponents.size, strideArray.size)) {
//            i += exponents[d]*strideArray[d]
//        }
//        return i
//    }


//    fun isOutOfBounds(exponents : IntArray) : Boolean {
//        return exponents.foldIndexed(false) { index, outOfBounds, d ->
//            outOfBounds || if(index < dim.size) d >= dim[index] else d > 0
//        }
//    }

}