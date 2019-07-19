package deselby.std.collections

import kotlin.math.min

class HashMultiset<T>(private val map : HashMap<T,Int> = HashMap()) : AbstractMutableSet<T>() {
    override var size : Int = 0


    constructor(container : Iterable<T>) : this() {
        container.forEach {
            add(it)
        }
    }


    constructor(container : Collection<T>) : this(HashMap(container.size)) {
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
        var removed = 0
        map.compute(element) {_, count ->
            if(count == null) null else {
                if(count <= n) {
                    removed = count
                    null
                } else {
                    removed = n
                    count - n
                }
            }
        }
        size -= removed
        return removed == n
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


//    override fun equals(other: Any?): Boolean {
//        if(other is HashMultiset<*>) {
//            return this.map == other.map
//        }
//        return false
//    }


    override fun toString() = map.toString()


    private fun decrementSize() {--size}

    ////////////////////////////////////////////////////////
    // overrides of standard algorithms that are quicker
    // than the default implementations
    ////////////////////////////////////////////////////////
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


    fun union(other : HashMultiset<T>) : HashMultiset<T> {
        val result = HashMultiset(HashMap(map))
        other.map.entries.forEach { result.map.merge(it.key, it.value, Int::plus) }
        return result
    }


    class MultiMutableIterator<A>(val mapIterator : MutableIterator<MutableMap.MutableEntry<A,Int>>, val decrementSize : ()->Unit) : MutableIterator<A> {
        private var currentEntry : MutableMap.MutableEntry<A,Int>? = null
        private var nItems : Int = 0

        override fun remove() {
            val entry = currentEntry
            when {
                entry == null       -> throw(NoSuchElementException())
                entry.value == 1    -> mapIterator.remove()
                else                -> entry.setValue(entry.value - 1)
            }
            decrementSize()
        }

        override fun hasNext(): Boolean = mapIterator.hasNext() || nItems > 0

        override fun next(): A {
            if(nItems > 0) {
                --nItems
            } else {
                val nextEntry = mapIterator.next()
                currentEntry = nextEntry
                nItems = nextEntry.value-1
            }
            return currentEntry!!.key
        }

    }
}


fun <T> hashMultisetOf(vararg elements : T) : HashMultiset<T> {
    return HashMultiset(elements.asList())
}