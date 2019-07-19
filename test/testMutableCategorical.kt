import deselby.std.distributions.AbsMutableCategorical
import deselby.std.distributions.MutableCategorical
import org.junit.Test
import kotlin.math.abs
import kotlin.random.Random

class testMutableCategorical {
    val myChoose = MutableCategorical<Int>()

    @Test
    fun testMutability() {
        myChoose.clear()
        val nItems = 500
        val probs = Array(nItems) { Random.nextDouble() }
        val map = HashMap<Int,Double>(nItems)
        myChoose.createHuffmanTree(1..nItems, probs.asList())
        probs.forEachIndexed {i, v ->
            map[i+1] = v
        }

        var total = 0.0
        for (q in 1..1000) {
            val toModify = Random.nextInt(nItems)
            if (Random.nextInt(3) < 2) {
                val newVal = Random.nextDouble()
                myChoose[toModify] = newVal
                map[toModify] = newVal
            } else {
                myChoose.remove(toModify)
                map.remove(toModify)
            }
        }
        assert(map.size == myChoose.size)
        var norm = 0.0
        for(e in map.entries) {
            assert(myChoose[e.key] == e.value)
            norm += e.value
        }

        val count = HashMap<Int,Int>(map.size)
        val nSamples = 10000000
        for (i in 1..nSamples) {
            val sample = myChoose.sample()
            count.compute(sample) {_, v -> (v?:0) + 1}
        }
        for(e in map.entries) {
            val p = (count[e.key]?:0) * 1.0/nSamples
            assert(abs(p - e.value/norm) < 0.0005)
            println("%d: %.4f -> %.4f".format(e.key, e.value/norm, p))
        }
    }

    @Test
    fun testSample() {
        myChoose.clear()

        val probs = listOf(0.6, 0.2, 0.1, 0.06, 0.04)
        for(i in 0..4) {
            myChoose[i] = probs[i]
        }

        val count = Array(myChoose.size) { 0 }
        val nSamples = 10000000
        for (i in 1..nSamples) {
            count[myChoose.sample()]++
        }
        val p = Array(myChoose.size) {count[it]*1.0/nSamples}
        for(i in 0 until probs.size) {
            val p = count[i]*1.0/nSamples
            assert(abs(p - probs[i]) < 0.001)
            println("%d: %.4f -> %.4f".format(i, myChoose[i], p))
        }
    }

    @Test
    fun testCreateBinaryTree() {
        myChoose.clear()

        val probs = listOf(0.6, 0.04, 0.06, 0.2, 0.1)
        // myChoose.createHuffmanTree(0..probs.size, probs)
        myChoose.createBinaryTree(0..probs.size, probs)

        val count = Array(myChoose.size) { 0 }
        val nSamples = 10000000
        for (i in 1..nSamples) {
            count[myChoose.sample()]++
        }
        for(i in 0 until probs.size) {
            val p = count[i]*1.0/nSamples
            assert(abs(p - probs[i]) < 0.001)
            println("%d: %.4f -> %.4f".format(i, myChoose[i], p))
        }
    }

    @Test
    fun testCreateHuffmanTree() {
        myChoose.clear()
        val probs = listOf(0.6, 0.04, 0.06, 0.2, 0.1)
        myChoose.createHuffmanTree(0..probs.size, probs)

        val count = Array(myChoose.size) { 0 }
        val nSamples = 10000000
        for (i in 1..nSamples) {
            count[myChoose.sample()]++
        }
        val p = Array(myChoose.size) {count[it]*1.0/nSamples}
        assert(p[0] > 0.599 && p[0] < 0.601)
        assert(p[3] > 0.199 && p[3] < 0.201)
        assert(p[4] > 0.099 && p[4] < 0.101)
        assert(p[2] > 0.059 && p[2] < 0.061)
        assert(p[1] > 0.039 && p[1] < 0.041)
        for (i in 0 until count.size) {
            println("%d: %.4f -> %.4f".format(i, myChoose[i], count[i] * 1.0 / nSamples))
        }


    }


    @Test
    fun testAddRemove() {
        myChoose.clear()

        myChoose[0] = 0.8
        assert(myChoose.size == 1)
        assert(myChoose[0] == 0.8)
        myChoose[1] = 0.2
        assert(myChoose.size == 2)
        assert(myChoose[0] == 0.8)
        assert(myChoose[1] == 0.2)
        myChoose[2] = 0.1
        assert(myChoose.size == 3)
        assert(myChoose[0] == 0.8)
        assert(myChoose[1] == 0.2)
        assert(myChoose[2] == 0.1)
        myChoose[3] = 0.1
        assert(myChoose.size == 4)
        assert(myChoose[0] == 0.8)
        assert(myChoose[1] == 0.2)
        assert(myChoose[2] == 0.1)
        assert(myChoose[3] == 0.1)
        myChoose[4] = 0.5
        assert(myChoose.size == 5)
        assert(myChoose[0] == 0.8)
        assert(myChoose[1] == 0.2)
        assert(myChoose[2] == 0.1)
        assert(myChoose[3] == 0.1)
        assert(myChoose[4] == 0.5)

        myChoose[0] = 0.6
        assert(myChoose.size == 5)
        assert(myChoose[0] == 0.6)
        assert(myChoose[1] == 0.2)
        assert(myChoose[2] == 0.1)
        assert(myChoose[3] == 0.1)
        assert(myChoose[4] == 0.5)

        println(myChoose)
        myChoose.remove(4)
        assert(myChoose.size == 4)
        assert(myChoose[0] == 0.6)
        assert(myChoose[1] == 0.2)
        assert(myChoose[2] == 0.1)
        assert(myChoose[3] == 0.1)
        println(myChoose)
        myChoose.remove(1)
        assert(myChoose.size == 3)
        assert(myChoose[0] == 0.6)
        assert(myChoose[2] == 0.1)
        assert(myChoose[3] == 0.1)
        println(myChoose)
        myChoose.remove(3)
        assert(myChoose.size == 2)
        assert(myChoose[0] == 0.6)
        assert(myChoose[2] == 0.1)
        println(myChoose)
        myChoose.remove(0)
        assert(myChoose.size == 1)
        assert(myChoose[2] == 0.1)
        println(myChoose)
        myChoose.remove(2)
        assert(myChoose.size == 0)
        println(myChoose)
    }

}