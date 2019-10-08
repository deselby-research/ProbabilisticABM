package experiments.phasedMonteCarlo

import deselby.fockSpace.*
import deselby.fockSpace.extensions.*
import deselby.std.extensions.nextExponential
import deselby.std.vectorSpace.OneHotDoubleVector
import deselby.std.vectorSpace.SamplableDoubleVector
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.random.Random




fun GroundedBasis<Int,Ground<Int>>.monteCarlo(hamiltonian: FockVector<Int>, T : Double) : OneHotDoubleVector<CreationBasis<Int>> {
    var time = 0.0
    var sampleWeight = 1.0

    val hIndex = hamiltonian.toAnnihilationIndex()
    var samplePhase = 1.0
    val sampleBasis = MutableCreationBasis(this.basis)
//    val groundBasis = sampleBasis on this.ground
    val possibleTransitionStates = SamplableDoubleVector(hamiltonian * this)
    do {
        val sampleRateOfChange = possibleTransitionStates[Basis.identity()]
        possibleTransitionStates.coeffs.remove(Basis.identity())

        val transitionRate = possibleTransitionStates.coeffs.sum()
        var timeToNextEvent = -ln(1.0 - Random.nextDouble()) / transitionRate // sum is rate of d change
        time += timeToNextEvent
        if(time > T) timeToNextEvent -= time - T
        val weightGrowthRate = transitionRate + sampleRateOfChange
        sampleWeight *= exp(weightGrowthRate*timeToNextEvent)

        if(time < T) {
            // choose perturbation
            val perturbation = possibleTransitionStates.sample()
            samplePhase *= perturbation.coeff
            possibleTransitionStates[Basis.identity()] = sampleRateOfChange
            // apply it to sampleBasis
            val reducedCommutation = hIndex.commute(perturbation.basis) * sampleBasis.asGroundedBasis(this.ground)
            possibleTransitionStates += reducedCommutation / perturbation.basis
            sampleBasis *= perturbation.basis
        }
    } while(time < T)
    return OneHotDoubleVector(sampleBasis, sampleWeight*samplePhase)
}


fun<AGENT> GroundedBasis<AGENT,Ground<AGENT>>.monteCarlo(hIndex: AnnihilationIndex<AGENT>, reducedHamiltonian: CreationVector<AGENT>, T : Double) : OneHotDoubleVector<CreationBasis<AGENT>> {
    var time = 0.0
    var sampleWeight = 1.0

    var samplePhase = 1.0
    val sampleBasis = MutableCreationBasis(this.basis)
    val possibleTransitionStates = SamplableDoubleVector(reducedHamiltonian)
    do {
        val sampleRateOfChange = possibleTransitionStates[Basis.identity()]
        possibleTransitionStates.coeffs.remove(Basis.identity())

        val transitionRate = possibleTransitionStates.coeffs.sum()
        var timeToNextEvent = Random.nextExponential(transitionRate) // sum is rate of d change
        time += timeToNextEvent
        if(time > T) timeToNextEvent -= time - T
        val weightGrowthRate = transitionRate + sampleRateOfChange
        sampleWeight *= exp(weightGrowthRate*timeToNextEvent)

        if(time < T) {
            // choose perturbation
            val perturbation = possibleTransitionStates.sample()
            samplePhase *= perturbation.coeff
            possibleTransitionStates[Basis.identity()] = sampleRateOfChange
            // apply it to sampleBasis
            val reducedCommutation = hIndex.commute(perturbation.basis) * sampleBasis.asGroundedBasis(this.ground)
            possibleTransitionStates += reducedCommutation / perturbation.basis
            sampleBasis *= perturbation.basis
        }
    } while(time < T)
    return OneHotDoubleVector(sampleBasis, sampleWeight*samplePhase)
}


// TEST of reverting possible transition states for next sample
fun<AGENT> GroundedBasis<AGENT,Ground<AGENT>>.monteCarlo(hIndex: AnnihilationIndex<AGENT>, possibleTransitionStates: SamplableDoubleVector<CreationBasis<AGENT>>, T : Double) : OneHotDoubleVector<CreationBasis<AGENT>> {
    var time = 0.0
    var sampleWeight = 1.0

    var samplePhase = 1.0
    val sampleBasis = MutableCreationBasis(this.basis)
    val originalTransitions = HashMap<CreationBasis<AGENT>, Double>()
    do {
        val sampleRateOfChange = possibleTransitionStates[Basis.identity()]

        val transitionRate = possibleTransitionStates.coeffs.sum() - abs(sampleRateOfChange)
        var timeToNextEvent = Random.nextExponential(transitionRate) // sum is rate of d change
        time += timeToNextEvent
        if(time > T) timeToNextEvent -= time - T
        val weightGrowthRate = transitionRate + sampleRateOfChange
        sampleWeight *= exp(weightGrowthRate*timeToNextEvent)

        if(time < T) {
            // choose perturbation
            possibleTransitionStates.coeffs.remove(Basis.identity())
            val perturbation = possibleTransitionStates.sample()
            possibleTransitionStates[Basis.identity()] = sampleRateOfChange
            samplePhase *= perturbation.coeff
            // apply it to sampleBasis
            val reducedCommutation = hIndex.commute(perturbation.basis) * sampleBasis.asGroundedBasis(this.ground)
            val transitionUpdate = reducedCommutation / perturbation.basis
            transitionUpdate.forEach {(basis, _) ->
                originalTransitions.computeIfAbsent(basis) {
                    possibleTransitionStates.getOrDefault(it,0.0)
                }
            }
            possibleTransitionStates += transitionUpdate
            sampleBasis *= perturbation.basis
        }
    } while(time < T)
    // revert to original value ready for next sample
    originalTransitions.forEach {(basis, originalWeight) ->
        if(originalWeight != 0.0)
            possibleTransitionStates[basis] = originalWeight
        else
            possibleTransitionStates.remove(basis)
    }
    return OneHotDoubleVector(sampleBasis, sampleWeight*samplePhase)
}


fun<AGENT> GroundedBasis<AGENT,Ground<AGENT>>.monteCarloIntegrate(hamiltonian: FockVector<AGENT>, integrationTime : Double,
                                                                  nSamples: Int,
                                                                  hIndex: AnnihilationIndex<AGENT> = hamiltonian.toAnnihilationIndex(),
                                                                  nThreads: Int = 8) : CreationVector<AGENT> {

    val reducedHamiltonian = hamiltonian * this

    val threadTotals = Array(nThreads) {
        GlobalScope.async {
            val total = HashCreationVector<AGENT>()
            val possibleTransitionStates = SamplableDoubleVector(reducedHamiltonian)
            val threadQuota = nSamples.div(nThreads) + if(it < nSamples.rem(nThreads)) 1 else 0
            for(i in 1..threadQuota) {
                val mcSample = this@monteCarloIntegrate.monteCarlo(hIndex, possibleTransitionStates, integrationTime)
                total += mcSample

            }
            total / nSamples.toDouble()
        }
    }

    val allTotal = runBlocking {
        val sum = HashCreationVector<AGENT>()
        threadTotals.forEach {
            sum += it.await()
        }
        sum
    }
    return allTotal
}

fun<AGENT> GroundedBasis<AGENT,Ground<AGENT>>.monteCarloIntegrateSingleThread(hamiltonian: FockVector<AGENT>, integrationTime : Double,
                                                                  nSamples: Int,
                                                                  hIndex: AnnihilationIndex<AGENT> = hamiltonian.toAnnihilationIndex()
                                                                  ) : CreationVector<AGENT> {

    val reducedHamiltonian = hamiltonian * this

    val total = HashCreationVector<AGENT>()
    val possibleTransitionStates = SamplableDoubleVector(reducedHamiltonian)
    for (i in 1..nSamples) {
        val mcSample = this.monteCarlo(hIndex, possibleTransitionStates, integrationTime)
        total += mcSample
    }
    return total / nSamples.toDouble()
}
