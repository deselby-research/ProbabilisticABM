package deselby.std.distributions

import kotlin.math.abs

class AbsMutableCategorical<T> : MutableCategorical<T> {
    constructor() : super()
    constructor(initialCapacity: Int) : super(initialCapacity)
    constructor(vararg categories: Pair<T, Double>) : super(*categories)


    override fun createLeaf(parent : InternalNode<T>?, category : T, probability : Double) =
            AbsLeafNode(parent, category, probability)


    fun signedSum() : Double {
        return leafNodes.values.sumByDouble { it.value }
    }


    class AbsLeafNode<T>(parent: InternalNode<T>?, item: T, probability: Double) : LeafNode<T>(parent, item, probability) {
        override val probability : Double
        get() = abs(value)
    }
}
