package experiments.phasedMonteCarlo

import deselby.fockSpaceV1.*
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sign
import kotlin.random.Random

fun MapFockState<Int>.monteCarloContinuous(H: (MapFockState<Int>) -> MapFockState<Int>, T : Double) : OneHotFock<Int> {
    var time = 0.0
    var sampleWeight = 1.0
    lateinit var sample : OneHotFock<Int>
    var possibleTransitionStates = SamplableFockState(this)
    do {
        sample = possibleTransitionStates.sample()
        val dp_dt = H(sample)
        val sampleRateOfChange = dp_dt.coeffs[sample.basis]?:0.0
        possibleTransitionStates = SamplableFockState(dp_dt - sample.basis * sampleRateOfChange)
        val transitionRate = possibleTransitionStates.coeffs.sum()
        var timeToNextEvent = -ln(1.0 - Random.nextDouble()) / transitionRate // sum is rate of state change
        if(time + timeToNextEvent >= T) timeToNextEvent = T - time
        time += timeToNextEvent
        val weightGrowthRate = transitionRate + sampleRateOfChange*sample.probability.sign
        sampleWeight *= exp(weightGrowthRate*timeToNextEvent)
    } while(time < T)
    return sample * sampleWeight
}

fun MapFockState<Int>.monteCarloTest(H: (MapFockState<Int>) -> MapFockState<Int>, T : Double) : OneHotFock<Int> {
    var time = 0.0
    var sampleWeight = 1.0

    val hamiltonian = H(OperatorBasis.identity<Int>().toFockState())
    val sampleBasis = MutableDeselbyBasis(this.coeffs.keys.first() as DeselbyBasis<Int>)
    val sampleAsPerturbation = DeselbyPerturbationBasis(sampleBasis)
    var sample = OneHotFock(sampleAsPerturbation)
    var possibleTransitionStates = SamplableFockState(hamiltonian * sample)
    do {
        val sampleRateOfChange = possibleTransitionStates[sampleAsPerturbation]
        possibleTransitionStates.coeffs.remove(sampleAsPerturbation)

        val transitionRate = possibleTransitionStates.coeffs.sum()
        var timeToNextEvent = -ln(1.0 - Random.nextDouble()) / transitionRate // sum is rate of state change
        time += timeToNextEvent
        if(time > T) timeToNextEvent -= time - T
        val weightGrowthRate = transitionRate + sampleRateOfChange*sample.probability.sign
        sampleWeight *= exp(weightGrowthRate*timeToNextEvent)

        if(time < T) {
            val perturbation = possibleTransitionStates.sample()
            val basis = perturbation.basis as DeselbyPerturbationBasis<Int>
            // apply it to sampleBasis
            basis.creations.forEach { sampleBasis.createAssign(it.key, it.value) }
            sample = OneHotFock(sampleAsPerturbation, perturbation.probability)
            possibleTransitionStates = SamplableFockState(hamiltonian * sample)
        }
    } while(time < T)
    return OneHotFock(sampleBasis, sampleWeight * sample.probability)
}


fun MapFockState<Int>.perturbativeMonteCarlo(H: (FockState<Int, MapFockState<Int>>) -> MapFockState<Int>, T : Double) : OneHotFock<Int> {
    var time = 0.0
    var sampleWeight = 1.0

    val hamiltonian = H(OperatorBasis.identity<Int>().toFockState())
    val commutations = CreationCommutations(hamiltonian)
    val sampleBasis = MutableDeselbyBasis(this.coeffs.keys.first() as DeselbyBasis<Int>)
    val sampleAsPerturbation = DeselbyPerturbationBasis(sampleBasis)
    val sample = OneHotFock(sampleAsPerturbation)
    var samplePhase = 1.0
    val possibleTransitionStates = SamplableFockState(hamiltonian * sampleAsPerturbation.toFockState())
    do {
        val sampleRateOfChange = possibleTransitionStates[sampleAsPerturbation]
        possibleTransitionStates.coeffs.remove(sampleAsPerturbation)

        val transitionRate = possibleTransitionStates.coeffs.sum()
//        println(possibleTransitionStates.coeffs.entries)
//        println(transitionRate)
        var timeToNextEvent = -ln(1.0 - Random.nextDouble()) / transitionRate // sum is rate of state change
        time += timeToNextEvent
        if(time > T) timeToNextEvent -= time - T
        val weightGrowthRate = transitionRate + sampleRateOfChange
        sampleWeight *= exp(weightGrowthRate*timeToNextEvent)

        if(time < T) {
            // choose perturbation
            val perturbation = possibleTransitionStates.sample()
            samplePhase *= perturbation.probability
            possibleTransitionStates[sampleAsPerturbation] = sampleRateOfChange
            val basis = perturbation.basis as DeselbyPerturbationBasis<Int>
            // apply it to sampleBasis
            basis.creations.forEach {
                val commutation: MapFockState<Int> = commutations[it.key] ?: ZeroFockState()
                if (it.value > 0) {
                    for (i in 1..it.value) {
                        val Q = commutation * sample
                        possibleTransitionStates -= Q
                        sampleBasis.createAssign(it.key, 1)
                    }
                } else if (it.value < 0) {
                    for (i in 1..-it.value) {
                        sampleBasis.createAssign(it.key, -1)
                        val Q = commutation * sample
                        possibleTransitionStates += Q
                    }
                }
            }
        }
    } while(time < T)
    return OneHotFock(sampleBasis, sampleWeight * samplePhase)
}


