package deselby.fockSpace


open class OperatorSet<AGENT>(val map : MutableMap<AGENT,Int>) {

    constructor(entries: Sequence<Map.Entry<AGENT,Int>>) : this(HashMap()) {
        entries.forEach {
            this[it.key] = it.value
        }
    }

    fun add(d : AGENT, n : Int) {
        map.merge(d, n) {a , b ->
            val newVal = a + b
            if(newVal == 0) null else newVal
        }
    }

    fun unionAssign(other: Sequence<Map.Entry<AGENT,Int>>) {
        other.forEach {
            map.merge(it.key, it.value) {a , b ->
                val newVal = a + b
                if(newVal == 0) null else newVal
            }
        }
    }

    operator fun get(d: AGENT): Int {
        return map.getOrDefault(d, 0)
    }


    operator fun set(d: AGENT, n : Int) {
        map.set(d, n)
    }


    override fun hashCode(): Int {
        return map.hashCode()
    }


    override fun equals(other: Any?): Boolean {
        if (other !is OperatorSet<*>) return false
        return (map == other.map)
    }
}