package experiments.phasedMonteCarlo

import deselby.std.extensions.nextCategorical
import org.junit.Test
import kotlin.math.abs
import kotlin.math.sign
import kotlin.random.Random

class stabilityTest {

    // test exponential growth of sample weight characteristics
    // for simple 3D system
    @Test
    fun stabilityTest() {
        val H = arrayOf(
                arrayOf(0.0, 0.0, 0.0),
                arrayOf(1.0, 0.0, 0.0),
                arrayOf(0.0, 0.0, 0.0)
                )

        val P = arrayOf(1.0,0.0,0.0)

        println("Timestepping")
        val dt = 0.01
        for(t in 1..100) {
            val dP = H*P
            for(i in 0 until 3) {
                P[i] += dP[i]*dt
            }
        }
        println(P.asList())

        println("sampling")
        val nSamples = 100000
        val samples = Array(nSamples) {
            arrayOf(1.0/nSamples,0.0,0.0)
        }
        samples.forEach { sample ->
            for(t in 1..1600) {
                val w = sample.sum()
                val a = when {
                    abs(sample[0]) > 0.0 -> 0
                    abs(sample[1]) > 0.0 -> 1
                    else -> 2
                }
                sample[a] = 1.0
                val dP = H*sample
                val choices = Array(3) {
                    if(it == a) 1.0 + dP[a]*dt else abs(dP[it]*dt)
                }
                val S = choices.sum()
                val nextSamplei = Random.nextCategorical(choices[0], choices[1], choices[2])
                if(nextSamplei == a) {
                    sample[a] = w*S
                } else {
                    sample[a] = 0.0
                    sample[nextSamplei] = w * S * sign(dP[nextSamplei])
                }
            }
        }
        val sampleSum = Array(3) { i ->
            samples.sumByDouble { sample -> sample[i] }
        }
        println(sampleSum.asList())
//        samples.forEach {
//            println(it.asList())
//        }
    }


    // matrix multiply
    operator fun Array<Array<Double>>.times(vec: Array<Double>) =
            Array(this.size) { i ->
                this[i].foldIndexed(0.0) {j, tot, Mij -> tot + vec[j]*Mij}
            }

}

