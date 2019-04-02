import deselby.std.nextPoisson
import org.apache.commons.math3.random.MersenneTwister
import kotlin.system.measureTimeMillis

fun main(args : Array<String>) {
    val rand = MersenneTwister()
    println(measureTimeMillis {
        for (i in 1..1000000) {
            val k = rand.nextPoisson(500.0)
    //   println(k)
        }
    })
}
