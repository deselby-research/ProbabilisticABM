package deselby.std.distributions

import java.util.*
import kotlin.NoSuchElementException
import kotlin.collections.HashMap
import kotlin.random.Random

open class MutableCategorical<T> : AbstractMutableMap<T, Double> {
    private var sumTreeRoot: SumTreeNode<T>? = null
    protected val leafNodes: HashMap<T, LeafNode<T>>

    override val entries: MutableSet<MutableMap.MutableEntry<T, Double>>
        get() = MutableEntrySet()

    override val size: Int
        get() = leafNodes.size


    constructor() {
        leafNodes = HashMap()
    }


    constructor(initialCapacity: Int) {
        leafNodes = HashMap(initialCapacity)
    }


    // Sets this to be the Huffman tree of the given categories with the given probabilities
    // This creates an optimally efficient tree but runs in O(n log(n)) time as the entries
    // need to be sorted
    fun createHuffmanTree(categories: Iterable<T>, probabilities: Iterable<Double>, initialCapacity: Int = -1) {
        val heapComparator = Comparator<SumTreeNode<T>> { i, j -> i.value.compareTo(j.value) }
        if (initialCapacity > 0)
            createTree(categories, probabilities, PriorityQueue(initialCapacity, heapComparator))
        else
            createTree(categories, probabilities, PriorityQueue(calcCapacity(categories, probabilities), heapComparator))
        HashMap(leafNodes)
    }


    // Sets this to be a binary tree of the given categories with the given probabilities
    // This creates a tree with minimal total depth and runs in O(n) time
    fun createBinaryTree(categories: Iterable<T>, probabilities: Iterable<Double>, initialCapacity: Int = -1) {
        if (initialCapacity > 0)
            createTree(categories, probabilities, ArrayDeque(initialCapacity))
        else
            createTree(categories, probabilities, ArrayDeque(calcCapacity(categories, probabilities)))
    }


    override operator fun get(key: T): Double {
        return leafNodes.get(key)?.value ?: 0.0
    }


    override fun put(item: T, probability: Double): Double? {
        if (probability == 0.0) return remove(item)
        val existingNode = leafNodes[item]
        if (existingNode == null) {
            val newNode = createLeaf(null, item, probability)
            sumTreeRoot = sumTreeRoot?.add(newNode) ?: newNode
            leafNodes[item] = newNode
            return null
        }
        val newRoot = existingNode.remove()
        val oldProb = existingNode.value
        existingNode.value = probability
        sumTreeRoot = newRoot?.add(existingNode) ?: existingNode
        return oldProb
    }


    operator fun set(item: T, probability: Double) = put(item, probability)


    override fun remove(item: T): Double? {
        val node = leafNodes.remove(item) ?: return null
        sumTreeRoot = node.remove()
        return node.value
    }


    fun sample(sum: Double): T? {
        return sumTreeRoot?.find(sum)?.key
    }


    fun sample(): T {
        val root = sumTreeRoot ?: throw(NoSuchElementException())
        return sample(Random.nextDouble() * root.value)!!
    }


    fun sum() = sumTreeRoot?.value ?: 0.0


    override fun clear() {
        leafNodes.clear()
        sumTreeRoot = null
    }


    fun calcHuffmanLength(): Double {
        return (sumTreeRoot?.calcHuffmanLength() ?: 0.0) / (sumTreeRoot?.value ?: 1.0)
    }


    private fun <Q : Queue<SumTreeNode<T>>> createTree(categories: Iterable<T>, probabilities: Iterable<Double>, heap: Q) {
        val category = categories.iterator()
        val probability = probabilities.iterator()

        clear()
        while (category.hasNext() && probability.hasNext()) {
            val prob = probability.next()
            val cat = category.next()
            if (prob > 0.0) {
                val newNode = createLeaf(null, cat, prob)
                heap.add(newNode)
                leafNodes[newNode.key] = newNode
            }
        }

        while (heap.size > 1) {
            val first = heap.poll()
            val second = heap.poll()
            val parent = createNode(null, first, second)
            heap.add(parent)
        }
        sumTreeRoot = heap.poll()
    }


    private fun calcCapacity(categories: Iterable<T>, probabilities: Iterable<Double>): Int {
        return when {
            categories is Collection<T> -> categories.size
            probabilities is Collection<Double> -> probabilities.size
            else -> 1024
        }
    }

    open fun createLeaf(parent : InternalNode<T>?, category : T, probability : Double) : LeafNode<T> =
            LeafNode(parent, category, probability)

    open fun createNode(parent : InternalNode<T>?, child1: SumTreeNode<T>, child2: SumTreeNode<T>) =
            InternalNode(parent, child1, child2)


    open class InternalNode<T> : SumTreeNode<T> {
        var leftChild: SumTreeNode<T>
        var rightChild: SumTreeNode<T>


        constructor(parent: InternalNode<T>?, child1: SumTreeNode<T>, child2: SumTreeNode<T>) : super(parent, child1.value + child2.value) {
            if (child1.value > child2.value) {
                this.leftChild = child1
                this.rightChild = child2
            } else {
                this.leftChild = child2
                this.rightChild = child1
            }
            child1.parent = this
            child2.parent = this
        }

        constructor(parent: InternalNode<T>?, lChild: SumTreeNode<T>, rChild: SumTreeNode<T>, sum : Double) : super(parent, sum) {
            leftChild = lChild
            rightChild = rChild
        }


        override fun find(sum: Double): LeafNode<T> {
            if (sum <= leftChild.value) return leftChild.find(sum)
            return rightChild.find(sum - leftChild.value)
        }

        override fun updateSum() {
            value = leftChild.value + rightChild.value
            if (leftChild.value < rightChild.value) {
                val tmp = rightChild
                rightChild = leftChild
                leftChild = tmp
            }
        }

        override fun add(newNode: LeafNode<T>): InternalNode<T> {
            if (value <= newNode.value) { // add right here
                return newNode.growNewParentAndInsertAbove(this)
            }
            rightChild.add(newNode)
            return this
        }

        fun swapChild(oldChild: SumTreeNode<T>, newChild: SumTreeNode<T>) {
            if (oldChild == leftChild) {
                leftChild = newChild
                return
            } else if (oldChild == rightChild) {
                rightChild = newChild
                return
            }
            throw(IllegalStateException("trying to swap a child that isn't a child"))
        }


        fun otherChild(firstChild: SumTreeNode<T>): SumTreeNode<T> {
            return when (firstChild) {
                leftChild -> rightChild
                rightChild -> leftChild
                else -> throw(NoSuchElementException())
            }
        }

        // returns the root node
        fun removeSelfAnd(child : SumTreeNode<T>) : SumTreeNode<T> {
            val keepChild = otherChild(child)
            parent?.swapChild(this, keepChild)
            keepChild.parent = parent
            return keepChild.parent?.updateSumsToRoot() ?: keepChild
        }

        override fun calcHuffmanLength() =  value + leftChild.calcHuffmanLength() + rightChild.calcHuffmanLength()

    }


    open class LeafNode<T> : SumTreeNode<T>, Map.Entry<T, Double> {
        override val key: T

        constructor(parent: InternalNode<T>?, item: T, probability: Double) : super(parent, probability) {
            this.key = item
        }

        override fun find(sum: Double): LeafNode<T> {
            return this
        }

        override fun add(newNode: LeafNode<T>) = newNode.growNewParentAndInsertAbove(this)


        fun growNewParentAndInsertAbove(insertPoint: SumTreeNode<T>): InternalNode<T> {
            val oldParent = insertPoint.parent
            val newParent = createInternalNode(insertPoint.parent, this, insertPoint)
            oldParent?.swapChild(insertPoint, newParent)
            newParent.parent?.updateSumsToRoot()
            return newParent
        }

        open fun createInternalNode(parent : InternalNode<T>?, child1: SumTreeNode<T>, child2: SumTreeNode<T>) =
                InternalNode(parent, child1, child2)

        // removes this and returns the root node
        fun remove(): SumTreeNode<T>? {
            val p = parent ?: return null
            return p.removeSelfAnd(this)
        }
    }


    abstract class SumTreeNode<T>(var parent: InternalNode<T>?, var value: Double) {

        // returns the root node
        fun updateSumsToRoot(): SumTreeNode<T> {
            updateSum()
            return parent?.updateSumsToRoot() ?: this
        }

        abstract fun add(newNode: LeafNode<T>): SumTreeNode<T>

        abstract fun find(sum: Double): LeafNode<T>

        open fun updateSum() {}

        open fun calcHuffmanLength() : Double = 0.0
    }


    inner class MutableEntrySet : AbstractMutableSet<MutableMap.MutableEntry<T, Double>>() {
        override fun add(element: MutableMap.MutableEntry<T, Double>): Boolean {
            this@MutableCategorical[element.key] = element.value
            return true
        }

        override val size: Int
            get() = this@MutableCategorical.size

        override fun iterator(): MutableIterator<MutableMap.MutableEntry<T, Double>> =
                MutableEntryIterator(this@MutableCategorical.leafNodes.iterator())

    }


    inner class MutableEntry(val leafNodeEntry: MutableMap.MutableEntry<T, LeafNode<T>>) :
            MutableMap.MutableEntry<T, Double> {
        override val key: T
            get() = leafNodeEntry.key
        override val value: Double
            get() = leafNodeEntry.value.value

        override fun setValue(newValue: Double): Double {
            val existingNode = leafNodeEntry.value
            val oldVal = existingNode.value
            val newRoot = existingNode.remove()
            existingNode.value = newValue
            sumTreeRoot = newRoot?.add(existingNode) ?: existingNode
            return oldVal
        }

        override fun hashCode(): Int {
            return key.hashCode() xor value.hashCode()
        }
    }


    inner class MutableEntryIterator(val leafNodesIterator: MutableIterator<MutableMap.MutableEntry<T, LeafNode<T>>>) :
            MutableIterator<MutableMap.MutableEntry<T, Double>> {
        var lastReturned: MutableMap.MutableEntry<T, LeafNode<T>>? = null
        override fun hasNext() = leafNodesIterator.hasNext()

        override fun next(): MutableMap.MutableEntry<T, Double> {
            val next = leafNodesIterator.next()
            lastReturned = next
            return MutableEntry(next)
        }

        override fun remove() {
            leafNodesIterator.remove()
            lastReturned?.value?.remove()
        }

    }


    override fun toString(): String {
        var s = "(  "
        for (item in entries) {
            s += "${item.key}->${item.value}  "
        }
        s += ")"
        return s
    }

}


fun <T> mutableCategoricalOf(vararg categories: Pair<T, Double>): MutableCategorical<T> {
    val d = MutableCategorical<T>(categories.size)
    d.createBinaryTree(
            categories.asSequence().map { it.first }.asIterable(),
            categories.asSequence().map { it.second }.asIterable(),
            categories.size
    )
    return d
}
