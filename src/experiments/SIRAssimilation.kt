package experiments

import deselby.MetropolisHastings
import deselby.MonteCarloRandomGenerator
import deselby.distributions.DeselbyDistribution
import deselby.distributions.FockState
import deselby.distributions.GeneratorPolynomial
import org.apache.commons.math3.distribution.BinomialDistribution
import org.apache.commons.math3.distribution.PoissonDistribution
import org.apache.commons.math3.random.MersenneTwister
import org.apache.commons.math3.random.RandomGenerator
import kotlin.math.abs
import kotlin.math.max

class SIRState(var S : Int, var I : Int, var R : Int) {
    constructor(p : GeneratorPolynomial) : this(
            p.annihilate(0).norm1().toInt(),
            p.annihilate(1).norm1().toInt(),
            p.annihilate(2).norm1().toInt()
    )
    fun toGeneratorPolynomial() = GeneratorPolynomial().create(0,S).create(1,I).create(2,R)
}

// Data assimilation into an SIR model (with no birth or death) using Deselby distributions
// and imperfect observation of the number of infected
fun main(args : Array<String>) {
    val observationInterval = 1.0
    val totalTime = 5.0
    val r = 0.9 // probability of detection of infected
    var realStartState = SIRState(35,5,0)
    val observations = generateObservations(realStartState, observationInterval, r, totalTime)

    var p = DeselbyDistribution(listOf(40.0, 7.0)) // initial prior
    var t = 0.0
    var nObs = 0
    while(t < totalTime) {
        p = p.integrateWithLambdaOptimisation(::SIRHamiltonian, observationInterval, 0.001)
     //   println("before observation :${p.marginaliseTo(1)}")
        println("Observing ${observations[nObs]}")
        p = p.binomialObserve(r, observations[nObs++], 1) //.truncateBelow(1e-5)
        p.renormalise()
        p = p.truncateBelow(1e-5)
        t += observationInterval
        println("after observation :${p.marginaliseTo(1)}")
        println(p.coeffs.fold(0.0) { a, b -> max(a, abs(b)) })
        println(p.dimension)
    }


}

fun metropolisHastingsPosterior() {
    val observationInterval = 1.0
    val totalTime = 5.0
    val r = 0.9 // probability of detection of infected
    var realStartState = SIRState(35,5,0)
    val observations = generateObservations(realStartState, observationInterval, r, totalTime)

    var mhRand = MonteCarloRandomGenerator()

//    MetropolisHastings {rand ->
//        val S = PoissonDistribution(40.0).sample()
//        val sim = SIRSimulate()
//        Pair()
//    }

}

fun generateObservations(s0 : SIRState, observationInterval : Double, detectionProb : Double, totalTime : Double) : Array<Int> {
    val sim = SIRSimulate(s0, observationInterval, totalTime)
    val observations = Array(sim.size) {i ->
        BinomialDistribution(sim[i].I, detectionProb).sample()
    }
    return observations
}


fun SIRSimulate(startState : SIRState, stepTime : Double, totalTime : Double, rand : RandomGenerator = MersenneTwister()) : ArrayList<SIRState> {
    val m = ArrayList<SIRState>((totalTime/stepTime + 1.0).toInt())
    var t = 0.0
    var p = startState.toGeneratorPolynomial()
    m.add(SIRState(p))
    while(t < totalTime) {
        val (dt,pPrime) = p.sampleNext(::SIRHamiltonian, rand)
        p = pPrime
        t += dt
        while(t > m.size*stepTime) {
            m.add(SIRState(p))
        }
    }
    return m
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
