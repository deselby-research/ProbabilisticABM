import deselby.std.distributions.AbsMutableCategorical
import org.junit.Test
import kotlin.math.abs
import kotlin.random.Random
import kotlin.system.measureTimeMillis

class testAbsMutableCategorical {
    val myChoose = AbsMutableCategorical<Int>()

    @Test
    fun testAbs() {
        myChoose.clear()
        myChoose[1] = -0.4
        myChoose[2] = 0.6
        println(myChoose)
        println(myChoose[1])
        println(myChoose[2])
        println(myChoose.sum())
        println(myChoose.signedSum())
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

    @Test
    fun testOneItem() {
        myChoose.clear()
        myChoose[1] = -0.5
        println(myChoose)
        println(myChoose[1])
        println(myChoose.sum())
    }
}