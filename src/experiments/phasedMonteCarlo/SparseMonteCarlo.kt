package experiments.phasedMonteCarlo

import deselby.fockSpace.FockState
import deselby.fockSpace.MapFockState
import deselby.fockSpace.OneHotFock
import deselby.fockSpace.SamplableFockState
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sign
import kotlin.random.Random

fun MapFockState<Int>.monteCarloContinuous(H: (FockState<Int, MapFockState<Int>>) -> MapFockState<Int>, T : Double) : OneHotFock<Int> {
    var time = 0.0
    var sampleWeight = 1.0
    lateinit var sample : OneHotFock<Int>
    var possibleTransitionStates = SamplableFockState(this)
    do {
        sample = possibleTransitionStates.sample()
        val dp_dt = H(sample)
        val sampleRateOfChange = dp_dt.coeffs[sample.basis]?:0.0
        possibleTransitionStates = SamplableFockState(dp_dt - sample.basis*sampleRateOfChange)

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
