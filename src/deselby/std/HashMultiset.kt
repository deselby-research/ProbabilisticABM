package deselby.std

class HashMultiset<T> {
    val map = HashMap<T,Int>()
    var size = 0


    fun add(m : T) {
        map.merge(m,1,Int::plus)
        size += 1
    }

    fun add(m : T, n : Int) {
        map.merge(m,n,Int::plus)
        size += n
    }

    fun remove(m : T) : Boolean {
        return remove(m,1)
    }

    fun remove(m : T, n : Int) : Boolean {
        val newCount = map.merge(m,-n,Int::plus)!!
        if(newCount < 0) {
            map.remove(m)
            return false
        }
        size -= n
        return true
    }

    fun elementAt(i : Int) : T {
        var count = i
        val it = map.iterator()
        var entry : MutableMap.MutableEntry<T,Int>
        do {
            entry = it.next()
            count -= entry.value
        } while(count>0)
        return entry.key
    }

    operator fun iterator() : MutableIterator<MutableMap.MutableEntry<T,Int>> {
        return map.iterator()
    }

    operator fun get(m : T) : Int {
        return map.get(m)?:0
    }

}