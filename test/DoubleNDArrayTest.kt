import deselby.std.DoubleNDArray
import org.junit.jupiter.api.Test

class DoubleNDArrayTest {
    @Test
    fun testIndexSet() {
        val x = DoubleNDArray(listOf(2,3,4), {0.0})
        for(index in x.indexSet) {
            println(index)
        }
    }
}