package deselby.mcmc

import org.apache.commons.math3.random.MersenneTwister
import org.apache.commons.math3.random.RandomGenerator
import kotlin.math.exp
import kotlin.math.sqrt

// supply a function from which you want to take samples. The function should take a RandomGenerator,
// from which it draws whatever random numbers it requires in order to create a sample from the Prior.
// Once you're done, return a Pair consisting of the log likelihood of the sample and the value of
// the measure you wish to take samples of.
//
// The Observations class can be used to help calculate the log likelihood. Just create an instance and
// call its members to assert observations. The log likelihood can then be taken from
// Observations.logp
//
// To start the MCMC sample process, call sampleToList. The samples can then be read
// from this MetropolisHastings object, as if from an ArrayList
class MetropolisHastings<T: Any>(val proposal: (MonteCarloRandomGenerator) -> MonteCarloRandomGenerator = {MonteCarloRandomGenerator.gaussianProposal(it)},
                            val model : (RandomGenerator) -> Pair<Double,T>
                            ) {
    val rand = MersenneTwister()

    fun sampleToList(numSamples : Int): List<T> {
        val samples = ArrayList<T>(numSamples)
        var mcRand = MonteCarloRandomGenerator()
        var lastLogProb = Double.NEGATIVE_INFINITY
        for(i in 0 until numSamples) {
            val perturbedRand = proposal(mcRand)
            val (newLogProb, sample) = model(perturbedRand)
            val logAlpha = newLogProb - lastLogProb
            if(logAlpha.isNaN() || rand.nextDouble() <= exp(logAlpha)) {
                samples.add(sample)
                lastLogProb = newLogProb
                mcRand = perturbedRand
            } else {
                samples.add(samples.last())
            }
        }
        return samples
    }

    fun sampleToSequence(numSamples : Int) : Sequence<T> {
        var mcRand = MonteCarloRandomGenerator()
        var logProb = Double.NEGATIVE_INFINITY
        var count = 0
        var sample: T? = null
        return generateSequence {
            if(count++ == numSamples) return@generateSequence null
            val perturbedRand = proposal(mcRand)
            val (proposedLogProb, proposedSample) = model(perturbedRand)
            val logAlpha = proposedLogProb - logProb
            if (logAlpha.isNaN() || rand.nextDouble() <= exp(logAlpha)) {
                logProb = proposedLogProb
                mcRand = perturbedRand
                sample = proposedSample
            }
            sample
        }
    }
}
