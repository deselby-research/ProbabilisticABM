package deselby.std.collections

class DoubleArrayListMap<K,V>(denseData : ArrayList<V>, keyToIndex : (K) -> Int, keysInOrder : Iterable<K>, paddingFactory : ()->V) : AbstractMutableMap<K,V>() {
    override val entries: EntrySet<K,V> = EntrySet(denseData, keyToIndex, keysInOrder, paddingFactory)
    val keyToIndex = entries.keyToIndex
    val keysInOrder = entries.keysInOrder

    constructor(arrayToCopy: DoubleArrayListMap<K,V>) : this(
            ArrayList(arrayToCopy.entries.denseData),
            arrayToCopy.entries.keyToIndex,
            arrayToCopy.entries.keysInOrder,
            arrayToCopy.entries.paddingFactory)

    constructor(keyToIndex : (K) -> Int, keysInOrder : Iterable<K>, paddingFactory : ()->V) : this(ArrayList(), keyToIndex, keysInOrder, paddingFactory)

    override fun put(key: K, value: V): V = entries.add(key,value)?:throw(IndexOutOfBoundsException())

    override fun get(key: K): V? = entries.get(key)

    fun asArray() = entries.denseData

    class EntrySet<K,V>(val denseData : ArrayList<V>, val keyToIndex : (K) -> Int, val keysInOrder : Iterable<K>, val paddingFactory : ()->V) : AbstractMutableSet<MutableMap.MutableEntry<K,V>>() {
        override val size: Int
            get() = denseData.size

        override fun add(element: MutableMap.MutableEntry<K, V>): Boolean {
            add(element.key, element.value)
            return true
        }

        fun add(key: K, value: V): V? {
            val index = keyToIndex(key)
            if(index >= denseData.size) {
                while(index > denseData.size) denseData.add(paddingFactory())
                denseData.add(value)
                return null
            }
            val oldVal = denseData[index]
            denseData[index] = value
            return oldVal
        }

        fun get(key : K) : V? {
            val index = keyToIndex(key)
            if(index >= denseData.size) return null
            return(denseData[index])
        }

        override fun iterator(): MutableIterator<Entry<K,V>> {
            return EntryIterator(0, keysInOrder.iterator())
        }


        class Entry<K,V>(override val key : K, override var value : V) : MutableMap.MutableEntry<K,V> {
            override fun setValue(newValue: V): V {
                val oldVal = value
                value = newValue
                return oldVal
            }
        }


        inner class EntryIterator(var index : Int, val keys : Iterator<K>) : MutableIterator<Entry<K,V>> {
            override fun hasNext()  = index < size-1
            override fun next()     = Entry(keys.next(), denseData[index++])
            override fun remove()   { throw(NotImplementedError()) }
        }
    }
}
