package deselby.std.distributions

import kotlin.math.abs

class AbsMutableCategorical<T> : MutableCategorical<T> {
    constructor() : super()
    constructor(initialCapacity: Int) : super(initialCapacity)

    override fun createLeaf(parent : InternalNode<T>?, category : T, probability : Double) =
            AbsLeafNode(parent, category, probability)


    override fun createNode(parent : InternalNode<T>?, child1: SumTreeNode<T>, child2: SumTreeNode<T>) =
            AbsInternalNode(parent, child1, child2)


    fun signedSum() : Double {
        return leafNodes.values.sumByDouble { it.value }
    }

    class AbsLeafNode<T> : LeafNode<T> {
        constructor(parent: InternalNode<T>?, item: T, probability: Double) : super(parent, item, probability)

        override fun createInternalNode(parent : InternalNode<T>?, child1: SumTreeNode<T>, child2: SumTreeNode<T>) =
            AbsInternalNode(parent, child1, child2)


    }

    class AbsInternalNode<T> : InternalNode<T> {
        constructor(parent: InternalNode<T>?, child1: SumTreeNode<T>, child2: SumTreeNode<T>) :
                super(parent, child1, child2, 0.0) {
            child1.parent = this
            child2.parent = this
            updateSum()
        }

        override fun updateSum() {
            val lValue = abs(leftChild.value)
            val rValue = abs(rightChild.value)
            value =  lValue + rValue
            if (lValue < rValue) {
                val tmp = rightChild
                rightChild = leftChild
                leftChild = tmp
            }
        }


        override fun find(sum: Double): LeafNode<T> {
            if (sum <= abs(leftChild.value)) return leftChild.find(sum)
            return rightChild.find(sum - abs(leftChild.value))
        }

        override fun add(newNode: LeafNode<T>): InternalNode<T> {
            if (abs(value) <= abs(newNode.value)) { // add right here
                return newNode.growNewParentAndInsertAbove(this)
            }
            rightChild.add(newNode)
            return this
        }


    }
}


fun <T> absMutableCategoricalOf(vararg categories: Pair<T, Double>): AbsMutableCategorical<T> {
    val d = AbsMutableCategorical<T>(categories.size)
    d.createBinaryTree(
            categories.asSequence().map { it.first }.asIterable(),
            categories.asSequence().map { it.second }.asIterable(),
            categories.size
    )
    return d
}
