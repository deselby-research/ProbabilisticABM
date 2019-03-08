package deselby.std

class NDIndexSet(private val dim : IntArray): Set<IntArray> {
    override val size: Int
        get() = dim.fold(1, {acc, v -> acc*v})

    val dimension : List<Int>
        get() = dim.asList()

    inner class NDIterator(private var index : IntArray) : Iterator<IntArray> {
        constructor() : this(IntArray(dim.size, { i -> if (i == 0) -1 else 0 }))

        override fun hasNext() =
                index.foldIndexed(false, { dimension, acc, i -> acc || (i < dim[dimension] - 1) })

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

    override fun contains(index: IntArray) = index.foldIndexed(true) { dimension, inBounds, d ->
        inBounds && d < dim[dimension]
    }

    override fun containsAll(elements: Collection<IntArray>) =
            elements.fold(true, {acc, index -> acc && contains(index)})

    override fun isEmpty() =
            dim.isEmpty() || dim.fold(false, {acc, i -> acc || i == 0})

    override fun iterator() = NDIterator()

}
