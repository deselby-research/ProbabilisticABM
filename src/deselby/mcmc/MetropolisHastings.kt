package deselby.mcmc

import org.apache.commons.math3.random.MersenneTwister
import org.apache.commons.math3.random.RandomGenerator
import kotlin.math.exp
import kotlin.math.sqrt

// supply a function from which you want to sample that takes a RandomGenerator and returns
// a pair consisting of the coeff and the value of the sample.
class MetropolisHastings<T>(val model : (RandomGenerator) -> Pair<Observations,T>) : ArrayList<T>() {
    val rand = MersenneTwister()

    fun sampleWithGaussianProposal(numSamples : Int, sigma : Double = 0.1) {
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

    fun expectation(f : (T) -> Double) = sumByDouble(f)/size
}

fun MetropolisHastings<out Number>.mean() : Double {
    return expectation { it.toDouble() }
}

fun MetropolisHastings<out Number>.standardDeviation() : Double {
    val mu  = mean()
    return sqrt(expectation {
        val delta = it.toDouble() - mu
        delta * delta
    })
}

