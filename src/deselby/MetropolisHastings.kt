package deselby

import org.apache.commons.math3.random.MersenneTwister
import org.apache.commons.math3.random.RandomGenerator
import kotlin.math.exp
import kotlin.math.sqrt

// supply a function from which you want to sample that takes a RandomGenerator and returns
// a pair consisting of the probability and the value of the sample.
class MetropolisHastings<T>(val model : (RandomGenerator) -> Pair<Observations,T>) : ArrayList<T>() {
    val rand = MersenneTwister()

    fun sampleWithGaussianProposal(numSamples : Int, sigma : Double) {
        var mcRand = MonteCarloRandomGenerator()
        clear()
        var lastLogProb = Double.NEGATIVE_INFINITY
        for(i in 0 until numSamples) {
            val perturbedRand = mcRand.perturbWithGaussian(sigma)
            val (obs, v) = model(perturbedRand)
            val newLogProb = obs.getLogP()
            val logAlpha = newLogProb - lastLogProb
            if(logAlpha.isNaN() || rand.nextDouble() <= exp(logAlpha)) {
                add(v)
                lastLogProb = newLogProb
                mcRand = perturbedRand
            } else {
                add(last())
            }
        }
    }

    inline fun expectation(f : (T) -> Double) = sumByDouble(f)/size
}

fun MetropolisHastings<Double>.mean() : Double {
    return expectation { it }
}

fun MetropolisHastings<Double>.standardDeviation() : Double {
    val mu  = mean()
    return sqrt(expectation {
        val delta = it - mu
        delta * delta
    })
}
