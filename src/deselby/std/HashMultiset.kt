package deselby.std

class HashMultiset<T>(private val map : HashMap<T,Int> = HashMap()) : AbstractMutableSet<T>() {
    override var size : Int = 0

    constructor(container : Iterable<T>) : this() {
        container.forEach {
            add(it)
        }
    }

    override fun add(m : T) = add(m,1)

    fun add(m : T, n : Int) : Boolean {
        map.merge(m,n,Int::plus)
        size += n
        return true
    }

    override fun remove(element : T) : Boolean {
        return remove(element,1)
    }

    fun remove(element : T, n : Int) : Boolean {
        val newCount = map.merge(element,-n,Int::plus)!!
        if(newCount <= 0) map.remove(element)
        if(newCount < 0) {
            size -= n + newCount
            return false
        }
        size -= n
        return true
    }

    fun elementAt(index : Int) : T {
        var count = index
        val it = map.iterator()
        var entry : MutableMap.MutableEntry<T,Int>
        do {
            entry = it.next()
            count -= entry.value
        } while(count>=0)
        return entry.key
    }

    fun memberCountMapEntries() = map.entries

    fun uniqueMembers() = map.keys

    fun memberCounts() = map.values

    override operator fun iterator() : MutableIterator<T> {
        return MultiMutableIterator(map.iterator(), this::decrementSize)
    }

    fun count(m : T) : Int {
        return map.getOrDefault(m,0)
    }

    override fun toString() = map.toString()

    private fun decrementSize() {--size}

    class MultiMutableIterator<A>(val mapIterator : MutableIterator<MutableMap.MutableEntry<A,Int>>, val decrementSize : ()->Unit) : MutableIterator<A> {
        private var currentEntry : MutableMap.MutableEntry<A,Int>? = null
        private var nItems : Int = 0

        override fun remove() {
            val entry = currentEntry
            when {
                entry == null       -> throw(IllegalStateException())
                entry.value == 1    -> mapIterator.remove()
                else                -> entry.setValue(entry.value - 1)
            }
            --nItems
            decrementSize()
        }

        override fun hasNext(): Boolean = mapIterator.hasNext() || nItems > 0

        override fun next(): A {
            if(nItems > 0) {
                --nItems
            } else {
                val nextEntry = mapIterator.next()
                currentEntry = nextEntry
                nItems = nextEntry.value
            }
            return currentEntry!!.key
        }

    }
}