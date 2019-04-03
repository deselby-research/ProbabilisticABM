import deselby.std.nextPoisson
import org.apache.commons.math3.random.MersenneTwister
import kotlin.math.exp
import kotlin.system.measureTimeMillis

fun main(args : Array<String>) {
    val rand = MersenneTwister()
    println(measureTimeMillis {
//        for (i in 1..1000000) {
//            rand.nextPoisson(99.0)
//        }

        val target = 1.0
        val lambda = 200.0
        var k = 0
        var P = exp(-lambda)
        var cumulativeP = P
        while(cumulativeP < target) {
            ++k
            P *= lambda/k
            cumulativeP += P
        }
        println("$k $cumulativeP $target")

    })
}
