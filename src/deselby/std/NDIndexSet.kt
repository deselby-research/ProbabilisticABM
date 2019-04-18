package deselby.std

class NDIndexSet(private val dim : IntArray): Set<IntArray> {
    override val size: Int
        get() = dim.fold(1) {acc, v -> acc*v}

    val dimension : List<Int>
        get() = dim.asList()

    inner class NDIterator(private var index : IntArray) : Iterator<IntArray> {
        constructor() : this(IntArray(dim.size) { i -> if (i == 0) -1 else 0 })

        override fun hasNext() =
                index.foldIndexed(false) { dimension, acc, i -> acc || (i < dim[dimension] - 1) }

        override fun next(): IntArray {
            var i = 0
            while (++index[i] == dim[i]) {
                index[i++] = 0
            }
            return index
        }

        override fun toString() = index.asList().toString()
    }

    constructor(size : Int, init : (Int) -> Int) : this(IntArray(size, init))

    override fun contains(element: IntArray) = element.foldIndexed(true) { dimension, inBounds, d ->
        inBounds && d < dim[dimension]
    }

    override fun containsAll(elements: Collection<IntArray>) =
            elements.fold(true) {acc, index -> acc && contains(index)}

    override fun isEmpty() =
            dim.isEmpty() || dim.fold(false) { acc, i -> acc || i == 0}

    override fun iterator() = NDIterator()

    fun rectangularUnion(other : NDIndexSet) : NDIndexSet {
        if(dimension.size != other.dimension.size)
            throw(IllegalArgumentException("Union of NDIndexSets with differing dimensionality: This is probably not what you intended to do"))
        return NDIndexSet(IntArray(dimension.size) { d ->
            kotlin.math.max(dimension[d], other.dimension[d])
        })
    }

    fun rectangularIntersection(other : NDIndexSet) : NDIndexSet {
        if(dimension.size != other.dimension.size)
            throw(IllegalArgumentException("Intersection of NDIndexSets with differing dimensionality: This is probably not what you intended to do"))
        return NDIndexSet(IntArray(dimension.size) { d ->
            kotlin.math.min(dimension[d], other.dimension[d])
        })
    }

    fun toStride() : IntArray {
        val stride = IntArray(dim.size) {1}
        for(i in 1 until stride.size) {
            stride[i] = stride[i-1]*dim[i-1]
        }
        return stride
    }
}
