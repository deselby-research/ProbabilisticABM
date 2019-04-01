package deselby

import org.apache.commons.math3.random.MersenneTwister
import org.apache.commons.math3.random.RandomGenerator
import kotlin.math.sqrt

// supply a function from which you want to sample that takes a RandomGenerator and returns
// a pair consisting of the probability and the value of the sample.
class MetropolisHastings<T>(val model : (RandomGenerator) -> Pair<Double,T>) : ArrayList<T>() {
    val rand = MersenneTwister()

    fun sampleWithGaussianProposal(numSamples : Int, sigma : Double) {
        var mcRand = MonteCarloRandomGenerator()
        clear()
        var lastProb = 0.0
        for(i in 0 until numSamples) {
            val perturbedRand = mcRand.perturbWithGaussian(sigma)
            val (prob, v) = model(perturbedRand)
//            println("lastProb = $lastProb")
            if(lastProb == 0.0 || rand.nextDouble() <= prob/lastProb) {
                add(v)
                lastProb = prob
                mcRand = perturbedRand
            } else {
                add(last())
            }
        }
    }

    inline fun expectation(f : (T) -> Double) = sumByDouble(f)/size
}

inline fun MetropolisHastings<Double>.mean() : Double {
    return expectation { it }
}

inline fun MetropolisHastings<Double>.standardDeviation() : Double {
    val mu  = mean()
    return sqrt(expectation {
        val delta = it - mu
        delta * delta
    })
}
