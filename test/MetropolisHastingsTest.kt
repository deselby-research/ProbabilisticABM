import deselby.mcmc.*
import org.junit.Test
import kotlin.math.abs
import kotlin.math.sqrt

class MetropolisHastingsTest {

    @Test
    fun GaussianSample() {
        val observation = 5.0
        val noiseSD = sqrt(2.0)
        val sampler = MetropolisHastings { rand ->
            val x = rand.nextGaussian() * sqrt(2.0) + 4.0
            val obs = Observations()
            obs.gaussian(x, noiseSD, observation)
            Pair(obs, x)
        }

        sampler.sampleWithGaussianProposal(100000, 0.1)
        val nmu = sampler.mean()
        val nsd = sampler.standardDeviation()
        println(nmu - 4.5)
        println(nsd - 1.0)
        assert(abs(nmu - 4.5) < 0.1)
        assert(abs(nsd - 1.0) < 0.05)
//       println(sampler)
//        printHistogram(2.5, 6.5, 80, sampler)
    }

    @Test
    fun randomGenerator() {
        val rand = MonteCarloRandomGenerator()
        val gaussian = DoubleArray(100000, {rand.nextGaussian()})
//        println(gaussian.asList())
        println(gaussian.sum()/gaussian.size)
        println(gaussian.sumByDouble({x -> x*x})/gaussian.size)
//        printHistogram(-4.0,4.0,40, gaussian.asList())
    }


    fun printHistogram(min : Double, max : Double, nBins : Int, data : List<Double>) {
        val bins = IntArray(nBins, {0})
        for(x in data) {
            val i = ((x-min)*nBins/(max-min)).toInt()
            if(i in 0 until nBins) {
                bins[i]++
            }
        }
        for(i in 0 until nBins) {
            val binVal = min + i*(max-min)/nBins
            println("$binVal ${bins[i]}")
        }
    }
}