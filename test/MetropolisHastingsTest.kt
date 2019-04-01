import deselby.MetropolisHastings
import deselby.MonteCarloRandomGenerator
import deselby.mean
import deselby.standardDeviation
import org.apache.commons.math3.distribution.NormalDistribution
import org.apache.commons.math3.random.MersenneTwister
import org.junit.jupiter.api.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.sqrt

class MetropolisHastingsTest {

    @Test
    fun GaussianSample() {
        val mu = 0.0
        val sd = 1.0
        val sampler = MetropolisHastings { rand ->
            val x = (rand.nextDouble()*2.0 - 1.0)*5.0*sd + mu
            val p = NormalDistribution(mu,sd).density(x)
            Pair(p, x)
        }

        sampler.sampleWithGaussianProposal(100000, 0.1)
        val nmu = sampler.mean()
        val nsd = sampler.standardDeviation()
        println(nmu - mu)
        println(nsd - sd)
        assert(abs(nmu - mu) < 0.1)
        assert(abs(nsd - sd) < 0.05)
//       println(sampler)
//        printHistogram(-4.0, 4.0, 80, sampler)
    }

    @Test
    fun randomGenerator() {
        val rand = MonteCarloRandomGenerator()
        val gaussian = DoubleArray(10000, {rand.nextGaussian()})
//        println(gaussian.asList())
        printHistogram(-4.0,4.0,40, gaussian.asList())
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