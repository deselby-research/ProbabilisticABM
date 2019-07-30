package deselby.std.collections

class DoubleArrayMap<K>(denseData : DoubleArray, keyToIndex : (K) -> Int, keysInOrder : Iterable<K>) : AbstractMutableMap<K,Double>() {
    override val entries: EntrySet<K> = EntrySet(denseData, keyToIndex, keysInOrder)
    val keyToIndex = entries.keyToIndex
    val keysInOrder = entries.keysInOrder

    constructor(arrayToCopy: DoubleArrayMap<K>) : this(
            arrayToCopy.entries.denseData.copyOf(),
            arrayToCopy.entries.keyToIndex,
            arrayToCopy.entries.keysInOrder)


    override fun put(key: K, value: Double): Double = entries.add(key,value)?:throw(IndexOutOfBoundsException())


    fun asArray() = entries.denseData


    class EntrySet<K>(val denseData : DoubleArray, val keyToIndex : (K) -> Int, val keysInOrder : Iterable<K>) : AbstractMutableSet<MutableMap.MutableEntry<K,Double>>() {
        override val size: Int
            get() = denseData.size

        override fun add(element: MutableMap.MutableEntry<K, Double>): Boolean {
            val index = keyToIndex(element.key)
            if(index >= denseData.size || index < 0) return false
            denseData[index] = element.value
            return true
        }

        fun add(key: K, value: Double): Double? {
            val index = keyToIndex(key)
            if(index >= denseData.size || index < 0) return null
            val oldVal = denseData[index]
            denseData[index] = value
            return oldVal
        }


        override fun iterator(): MutableIterator<Entry<K>> {
            return EntryIterator(0, keysInOrder.iterator())
        }


        class Entry<K>(override val key : K, override var value : Double) : MutableMap.MutableEntry<K,Double> {
            override fun setValue(newValue: Double): Double {
                val oldVal = value
                value = newValue
                return oldVal
            }
        }


        inner class EntryIterator(var index : Int, val keys : Iterator<K>) : MutableIterator<Entry<K>> {
            override fun hasNext()  = index < size-1
            override fun next()     = Entry(keys.next(), denseData[index++])
            override fun remove()   { throw(NotImplementedError()) }
        }
    }
}
