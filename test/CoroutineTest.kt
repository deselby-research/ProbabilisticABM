import deselby.std.extensions.nextPoisson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.apache.commons.math3.random.MersenneTwister
import org.junit.Test
import kotlin.system.measureTimeMillis

class CoroutineTest {

    @Test
    fun testConcurrency() {
        val nSamples = 1654321
        val nThreads = 8
        val results = Array(nThreads) {
            GlobalScope.async {
              //  val rand = MersenneTwister()
                var sum = 0.0
                val threadQuota = nSamples.div(nThreads) + if(it < nSamples.rem(nThreads)) 1 else 0
                for(i in 1..threadQuota) {
                    sum += 1.0
                }
                sum
            }
        }

        val t = measureTimeMillis {
            val total = runBlocking {
                var total = 0.0
                results.forEach {
                    total += it.await()
                }
                total
            }
            println("Total = $total")
        }

        println("time = $t")

    }
}