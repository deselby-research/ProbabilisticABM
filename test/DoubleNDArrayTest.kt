import deselby.std.DoubleNDArray
import org.junit.jupiter.api.Test

class DoubleNDArrayTest {
    @Test
    fun testIndexSet() {
        val x = DoubleNDArray(listOf(2,3,4), {0.0})
        for(index in x.indexSet) {
            println(index.asList())
        }
    }

    @Test
    fun testSlice() {
        val x = DoubleNDArray(listOf(3,4,5), {i -> i[0]*100.0 + i[1]*10.0 + i[2]})
        val mySlice = x.slice(1,2)
        println(mySlice)
        for(ndi in mySlice.indexSet) {
            assert(mySlice[ndi] == ndi[0]*100.0 + 20.0 + ndi[1])
        }
    }
}