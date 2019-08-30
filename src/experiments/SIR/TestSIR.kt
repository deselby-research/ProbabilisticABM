package experiments.SIR

import deselby.fockSpace.*
import deselby.fockSpace.extensions.*
import org.apache.commons.math3.random.MersenneTwister
import org.junit.Test

class TestSIR {
    val params = SIRParams(0.01, 0.1, 40.0, 7.0)
    val observationInterval = 1.0
    val totalTime = 3.0
    val r = 0.9 // coeff of detection of infected
    val realStartState = NonFockSIR.SIRState(35, 5, 0)
    val simulator = NonFockSIR.SIRSimulator(params, MersenneTwister())
    val observations = simulator.generateObservations(realStartState, observationInterval, r, totalTime)

    @Test
    fun testCommutation() {
        val H = FockSIR.Hamiltonian(params)
        val index = H.toAnnihilationIndex()
        println(H)
        println(index.entries)
        println()

        index.commute(CreationBasis<Int>().create(0)) { basis, weight ->
            println("$weight $basis")
        }
        println()
        index.commute(CreationBasis<Int>().create(1)) { basis, weight ->
            println("$weight $basis")
        }
    }

    @Test
    fun testBinomialMult() {
        val H = Basis.identityCreationVector<Int>().create(0, 2)
        val deselby = DeselbyGround(mapOf(0 to 0.1))
        val binomialLikelihood = BinomialLikelihood(0, 0.7, 3)
        println(binomialLikelihood * H.asGroundedVector(deselby))
    }

    @Test
    fun testNonFockPosterior() {
        println("Observations = ${observations.asList()}")
    }

    @Test
    fun testPosteriors() {
        println("Observations = ${observations.asList()}")
        NonFockSIR.MCMCPosterior(observations, observationInterval, params, r, 2000000)
        println()
//        DeselbySIR.posterior(observations, observationInterval, params, r)
//        println()
        FockSIR.posterior(observations, observationInterval, params, r)
        println()
//        FockSIR.monteCarloPosterior(observations, observationInterval, params, r)
    }

    @Test
    fun testPriors() {
        val time = 0.5
        val nonFockSimulator = NonFockSIR.SIRSimulator(params, MersenneTwister())
        nonFockSimulator.prior(1000000, time)
        println()
        DeselbySIR.prior(params,time)
        println()
        FockSIR.prior(params,time)
        println()
        FockSIR.monteCarloPrior(params, time, 10000000)
    }

    @Test
    fun testBinomialProduct() {
        val lambda = 0.2
        val p = 0.6
        println(FockSIR.binomialProduct(4, 3, p,lambda))
        println(DeselbySIR.binomialProduct(4, 3, p,lambda))
        println(DeselbySIR.binomialProductTest(4, 3, p,lambda))
    }

    @Test
    fun testFallingFactorial() {
        val lambda = 0.5
        println(FockSIR.fallingFactorial(1, 1,lambda))
        println(DeselbySIR.fallingFactorial(1, 1,lambda))
    }

}
