package deselby.std.vectorSpace

class EmptyDoubleVector<BASIS> : DoubleVector<BASIS> {

    override val entries: Set<Map.Entry<BASIS, Double>>
            get() = emptySet()
    override val keys: Set<BASIS>
            get() = emptySet()
    override val size: Int
            get() = 0
    override val values: Collection<Double>
        get() = emptySet()

    override fun containsKey(key: BASIS) = false
    override fun containsValue(value: Double) = false
    override fun get(key: BASIS) = null
    override fun isEmpty() = true

    override fun zero() = HashDoubleVector<BASIS>()
    override fun toMutableVector() = HashDoubleVector<BASIS>()

    override fun toString() = "{ }"
}