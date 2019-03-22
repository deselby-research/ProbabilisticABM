package experiments

import deselby.distributions.DeselbyDistribution
import deselby.distributions.FockState
import deselby.distributions.GeneratorPolynomial
import org.apache.commons.math3.distribution.BinomialDistribution
import java.util.*

// Data assimilation into an SIR model (with no birth or death) using Deselby distributions
// and imperfect observation of the number of infected
fun main(args : Array<String>) {
    val observationInterval = 5.0
    var reality = GeneratorPolynomial().create(0, 35).create(1,5)
    generateObservations(reality, observationInterval)


    var p = DeselbyDistribution(listOf(20.0, 40.0)) // initial prior

    println(SIRHamiltonian(p))

//    p = p.integrate(::SIRHamiltonian, 5.0, 0.005)

    println(p)
}

fun generateObservations(p0 : GeneratorPolynomial, observationInterval : Double) : ArrayList<Int> {
    val observations = ArrayList<Int>()
    var t = 0.0
    val r = 0.9 // probability of detection of infected
    var p = p0
    var lastObservation = 0.0
    while(t < 100) {
        val nInfected = p.annihilate(1).norm1().toInt()
//        println("$t ${p.annihilate(0).norm1()} ${p.annihilate(1).norm1()}")
        val (dt,pPrime) = p.sampleNext(::SIRHamiltonian)
        p = pPrime
        t += dt
        if(t-lastObservation > observationInterval) {
            val obsnInfected = BinomialDistribution(nInfected, r).sample()
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
    val beta = 0.01
    val gamma = 0.1
    val p0 = p.annihilate(1)
    val siSlector = (p0 * beta).annihilate(0)
    val infection2 = siSlector.create(0).create(1)
    val iSelector = p0 * gamma
    val recovery2 = iSelector.create(1)
    return siSlector.create(1).create(1) - infection2 + iSelector - recovery2
}
