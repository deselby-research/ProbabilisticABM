import deselby.std.distributions.AbsMutableCategorical
import deselby.std.distributions.MutableCategorical
import org.junit.Test
import kotlin.math.abs

class testAbsMutableCategorical {
    val myChoose = AbsMutableCategorical<Int>()

    @Test
    fun testAbs() {
        myChoose.clear()
        myChoose[1] = -0.5
        myChoose[2] = 0.5
        println(myChoose)
        println(myChoose.sum())
        assert(myChoose.sum() == 1.0)
        var count = 0
        val nSamples = 1000000
        for(i in 1..nSamples) {
            if(myChoose.sample() == 1) ++count
        }
        val p = count*1.0/nSamples
        println(p)
        assert(abs(p - abs(myChoose[1])) < 0.002)
    }
}