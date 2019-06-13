import deselby.std.HashMultiset
import org.junit.jupiter.api.Test

class HashMultisetTest {
    val data = arrayListOf(1, 4, 5, 5, 1, 6)

    @Test
    fun testAdd() {
        val hashset = HashMultiset<Int>()
        data.forEach {
            hashset.add(it)
        }
        assert(hashset.count(1) == 2)
        assert(hashset.count(4) == 1)
        assert(hashset.count(5) == 2)
        assert(hashset.count(6) == 1)
        assert(hashset.size == 6)
    }

    @Test
    fun testRemove() {
        val hashset = HashMultiset(data)
        println(hashset)

        hashset.remove(1)
        println(hashset)
        assert(hashset.count(1) == 1)
        assert(hashset.count(4) == 1)
        assert(hashset.count(5) == 2)
        assert(hashset.count(6) == 1)
        assert(hashset.size == 5)

        hashset.remove(4)
        println(hashset)
        assert(hashset.count(1) == 1)
        assert(hashset.count(5) == 2)
        assert(hashset.count(6) == 1)
        assert(hashset.size == 4)
        assert(hashset.uniqueMembers().size == 3)

        hashset.removeIf {it == 1}
        println(hashset)
        assert(hashset.count(6) == 1)
        assert(hashset.count(5) == 2)
        assert(hashset.size == 3)
        assert(hashset.uniqueMembers().size == 2)

        hashset.removeIf {it == 5}
        println(hashset)
        assert(hashset.count(6) == 1)
        assert(hashset.size == 1)
        assert(hashset.uniqueMembers().size == 1)

        hashset.removeIf {it == 6}
        println(hashset)
        assert(hashset.size == 0)
        assert(hashset.uniqueMembers().size == 0)

    }

    @Test
    fun testElementAt() {
        val hs = HashMultiset(data)
        println(Array(6) {
            hs.elementAt(it)
        }.asList())
        println(data.sorted())
        data.sorted().forEachIndexed {index, element ->
            assert(element == hs.elementAt(index))
        }

    }
}