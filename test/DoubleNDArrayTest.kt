import deselby.std.collections.DoubleNDArray
import org.junit.Test

class DoubleNDArrayTest {
    @Test
    fun testIndexSet() {
        val x = DoubleNDArray(listOf(2, 3, 4), { it[0] * 100.0 + it[1] * 10.0 + it[2] * 1.0 })
        for(index in x.indexSet) {
            println("${index.asList()} -> ${x[index]}")
        }
    }

    @Test
    fun testSlice() {
        val x = DoubleNDArray(listOf(3, 4, 5), { i -> i[0] * 100.0 + i[1] * 10.0 + i[2] })
        val slice = x.indexSet
        slice[1] = 2..2
        val mySlice = x[slice]
        println(mySlice)
        mySlice.size
        for(ndi in mySlice.indexSet) {
            assert(mySlice[ndi] == ndi[0]*100.0 + 20.0 + ndi[2])
        }
    }
}