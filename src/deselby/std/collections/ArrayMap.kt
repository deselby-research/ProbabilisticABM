package deselby.std.collections

class ArrayMap<K,V>(denseData : Array<V>, keyToIndex : (K) -> Int, keysInOrder : Iterable<K>) : AbstractMutableMap<K,V>() {
    override val entries: EntrySet<K, V> = EntrySet(denseData, keyToIndex, keysInOrder)
    val keyToIndex = entries.keyToIndex
    val keysInOrder = entries.keysInOrder

    constructor(arrayToCopy: ArrayMap<K, V>) : this(
            arrayToCopy.entries.denseData.copyOf(),
            arrayToCopy.entries.keyToIndex,
            arrayToCopy.entries.keysInOrder)


    override fun put(key: K, value: V): V = entries.add(key,value)?:throw(IndexOutOfBoundsException())


    fun asArray() = entries.denseData


    class EntrySet<K,V>(val denseData : Array<V>, val keyToIndex : (K) -> Int, val keysInOrder : Iterable<K>) : AbstractMutableSet<MutableMap.MutableEntry<K,V>>() {
        override val size: Int
            get() = denseData.size

        override fun add(element: MutableMap.MutableEntry<K, V>): Boolean {
            val index = keyToIndex(element.key)
            if(index >= denseData.size || index < 0) return false
            denseData[index] = element.value
            return true
        }

        fun add(key: K, value: V): V? {
            val index = keyToIndex(key)
            if(index >= denseData.size || index < 0) return null
            val oldVal = denseData[index]
            denseData[index] = value
            return oldVal
        }


        override fun iterator(): MutableIterator<Entry<K, V>> {
            return EntryIterator(0, keysInOrder.iterator())
        }


        class Entry<K,V>(override val key : K, override var value : V) : MutableMap.MutableEntry<K,V> {
            override fun setValue(newValue: V): V {
                val oldVal = value
                value = newValue
                return oldVal
            }
        }


        inner class EntryIterator(var index : Int, val keys : Iterator<K>) : MutableIterator<Entry<K, V>> {
            override fun hasNext()  = index < size-1
            override fun next()     = Entry(keys.next(), denseData[index++])
            override fun remove()   { throw(NotImplementedError()) }
        }
    }
}
