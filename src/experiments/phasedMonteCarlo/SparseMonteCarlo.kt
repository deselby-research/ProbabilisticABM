package experiments.phasedMonteCarlo

import experiments.fockBasis.AbstractFockState
import experiments.fockBasis.FockState
import experiments.fockBasis.OneHotFock
import experiments.fockBasis.SamplableFockDecomposition
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sign
import kotlin.random.Random

fun AbstractFockState<Int>.monteCarloContinuous(H: (AbstractFockState<Int>) -> AbstractFockState<Int>, T : Double) : OneHotFock<Int> {
    var time = 0.0
    var sampleWeight = 1.0
    lateinit var sample : OneHotFock<Int>
    var possibleTransitionStates = SamplableFockDecomposition(this)
    do {
        sample = possibleTransitionStates.sample()
        val dp_dt = H(sample)
        val sampleRateOfChange = dp_dt.coeffs[sample.basis]?:0.0
        possibleTransitionStates = SamplableFockDecomposition(dp_dt - sample.basis*sampleRateOfChange)

//        possibleTransitionStates.setToZero()
//        possibleTransitionStates += dp_dt
//        possibleTransitionStates.coeffs.remove(sample.basis)

//        val transitionRate = possibleTransitionStates.coeffs.values.sumByDouble(::abs)
        val transitionRate = possibleTransitionStates.coeffs.sum()
        var timeToNextEvent = -ln(1.0 - Random.nextDouble()) / transitionRate // sum is rate of state change
        if(time + timeToNextEvent >= T) timeToNextEvent = T - time
        time += timeToNextEvent
        val weightGrowthRate = transitionRate + sampleRateOfChange*sample.probability.sign
        sampleWeight *= exp(weightGrowthRate*timeToNextEvent)
    } while(time < T)
    return sample * sampleWeight
}
