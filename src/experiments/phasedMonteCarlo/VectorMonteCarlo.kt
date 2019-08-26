package experiments.phasedMonteCarlo

import deselby.fockSpace.*
import deselby.fockSpace.extensions.*
import deselby.fockSpaceV2.Deselby
import deselby.fockSpaceV2.DeselbyPerturbation
import deselby.fockSpaceV2.Operator
import deselby.fockSpaceV2.extensions.*
import deselby.std.extensions.nextExponential
import deselby.std.vectorSpace.DoubleVector
import deselby.std.vectorSpace.EmptyDoubleVector
import deselby.std.vectorSpace.OneHotDoubleVector
import deselby.std.vectorSpace.SamplableDoubleVector
import kotlin.math.exp
import kotlin.math.ln
import kotlin.random.Random


fun OneHotDoubleVector<Deselby<Int>>.perturbativeMonteCarlo(hamiltonian: DoubleVector<Operator<Int>>, T : Double) : OneHotDoubleVector<Deselby<Int>> {
    var time = 0.0
    var sampleWeight = 1.0

//    val hamiltonian = H(OperatorBasis.identity<Int>().toFockState())
    val commutations = hamiltonian.toCreationCommutationMap()
    val sampleBasis = Deselby(this.basis)
    val sampleAsPerturbation = DeselbyPerturbation(sampleBasis)
    val sample = OneHotDoubleVector(sampleAsPerturbation,1.0)
    var samplePhase = 1.0
    val possibleTransitionStates = SamplableDoubleVector(hamiltonian * sample)
    do {
        val sampleRateOfChange = possibleTransitionStates[sampleAsPerturbation]
        possibleTransitionStates.coeffs.remove(sampleAsPerturbation)

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
            possibleTransitionStates[sampleAsPerturbation] = sampleRateOfChange
            // apply it to sampleBasis
            perturbation.basis.creations.forEach {
                val commutation = commutations[it.key] ?: EmptyDoubleVector<Operator<Int>>()
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
    return OneHotDoubleVector(sampleBasis, sampleWeight*samplePhase)
}


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
//    val groundBasis = sampleBasis on this.ground
    val possibleTransitionStates = SamplableDoubleVector(reducedHamiltonian)
    do {
//        println()
//        println("possibleTransitions = $possibleTransitionStates  ${samplePhase}")
        val sampleRateOfChange = possibleTransitionStates[Basis.identity()]
//        println("sampleRateOfChange = $sampleRateOfChange")
        possibleTransitionStates.coeffs.remove(Basis.identity())

        val transitionRate = possibleTransitionStates.coeffs.sum()
        var timeToNextEvent = Random.nextExponential(transitionRate) // sum is rate of d change
        time += timeToNextEvent
        if(time > T) timeToNextEvent -= time - T
        val weightGrowthRate = transitionRate + sampleRateOfChange
        sampleWeight *= exp(weightGrowthRate*timeToNextEvent)
//        println("time = $time")
//        println("sampleWeight = $sampleWeight  growthRate = $weightGrowthRate")

        if(time < T) {
            // choose perturbation
            val perturbation = possibleTransitionStates.sample()
            samplePhase *= perturbation.coeff
//            println("samplePhase = ${samplePhase}")
            possibleTransitionStates[Basis.identity()] = sampleRateOfChange
//            println("sampleRateOfChange = $sampleRateOfChange")
//            println("possibleTransitions = $possibleTransitionStates")
            // apply it to sampleBasis
            val reducedCommutation = hIndex.commute(perturbation.basis) * sampleBasis.asGroundedBasis(this.ground)
            possibleTransitionStates += reducedCommutation / perturbation.basis
            sampleBasis *= perturbation.basis
//            println("perturbation = $perturbation")
//            println("sample = $sampleBasis")
        }
    } while(time < T)
    return OneHotDoubleVector(sampleBasis, sampleWeight*samplePhase)
}
