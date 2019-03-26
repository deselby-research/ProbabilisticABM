package experiments

import deselby.distributions.DeselbyDistribution
import deselby.distributions.FockState
import deselby.distributions.GeneratorPolynomial
import org.apache.commons.math3.distribution.BinomialDistribution
import java.util.*
import kotlin.math.abs
import kotlin.math.max

// Data assimilation into an SIR model (with no birth or death) using Deselby distributions
// and imperfect observation of the number of infected
fun main(args : Array<String>) {
    val observationInterval = 1.0
    val totalTime = 5.0
    val r = 0.9 // probability of detection of infected
    var reality = GeneratorPolynomial().create(0, 35).create(1,5)
    val observations = generateObservations(reality, observationInterval, r, totalTime)


    var p = DeselbyDistribution(listOf(40.0, 7.0)) // initial prior


//    println(SIRHamiltonian(p))

    var t = 0.0
    while(t < totalTime) {
        p = p.integrateWithLambdaOptimisation(::SIRHamiltonian, observationInterval, 0.001)
        println("before observation :$p")
        p = p.binomialObserve(r, observations[0], 1) //.truncateBelow(1e-5)
        p.renormalise()
        p = p.truncateBelow(1e-5)
        t += observationInterval
        println("after observation :$p")
        println(p.coeffs.fold(0.0, { a, b -> max(a, abs(b)) }))
        println(p.dimension)
    }


}

fun generateObservations(p0 : GeneratorPolynomial, observationInterval : Double, detectionProb : Double, totalTime : Double) : ArrayList<Int> {
    val observations = ArrayList<Int>()
    var t = 0.0
    var p = p0
    var lastObservation = 0.0
    while(t < totalTime) {
        val nInfected = p.annihilate(1).norm1().toInt()
//        println("$t ${p.annihilate(0).norm1()} ${p.annihilate(1).norm1()}")
        val (dt,pPrime) = p.sampleNext(::SIRHamiltonian)
        p = pPrime
        t += dt
        if(t-lastObservation > observationInterval) {
            val obsnInfected = BinomialDistribution(nInfected, detectionProb).sample()
            println("$nInfected $obsnInfected")
            observations.add(obsnInfected)
            lastObservation += observationInterval
        }
    }
//    println("$t ${p.annihilate(0).norm1()} ${p.annihilate(1).norm1()}")
    return observations
}


// H = beta(c_i^2  - c_s c_i)a_s a_i + gamma(1 - c_i)a_i
fun <D : FockState<Int,D>> SIRHamiltonian(p : FockState<Int,D>) : D {
    val beta = 0.01 // rate of infection per si pair
    val gamma = 0.1 // rate of recovery per person
    val p0 = p.annihilate(1)
    val siSlector = (p0 * beta).annihilate(0)
    val infection2 = siSlector.create(0).create(1)
    val iSelector = p0 * gamma
    val recovery2 = iSelector.create(1)
    return siSlector.create(1).create(1) - infection2 + iSelector - recovery2
}
