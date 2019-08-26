package experiments.spatialPredatorPrey.fock

import deselby.fockSpace.Basis
import deselby.fockSpace.CreationBasis
import deselby.fockSpace.GroundedBasis
import deselby.fockSpace.extensions.asGroundedVector
import org.junit.Test

class Experiments {

    @Test
    fun simulate() {
        val integrationTime = 1.0
        val sim = Simulation(0.01,0.02)
        val startState = CreationBasis<Agent>(emptyMap()) //Basis.identity<Agent>()
//        val sample= GroundedBasis(startState, sim.D0)

//    val p = sample.integrate(sim.H, integrationTime, 0.001, 1e-8)
//    println("integral = $p")
        println()

        val nSamples = 1000
        val finalState = sim.monteCarloIntegrate(startState, nSamples, integrationTime)
        println("final state = $finalState")
        println("nTerms = ${finalState.size}")
        println("normalisation = ${finalState.values.sum()}")
    }

    @Test
    fun assimilate() {
        val lambdaPred = 0.1
        val lambdaPrey = 0.2
        val obsInterval = 1.0
        val pObserve = 0.5
        val nObservations = 4
        val sim = Simulation(lambdaPred,lambdaPrey)
        var sample= Basis.identity<Agent>()
        val observations = ObservationGenerator.generate(lambdaPred, lambdaPrey, pObserve, nObservations, obsInterval)

        println("prior ground = ${sim.D0}")

        val nSamples = 10000
        observations.forEach { (realState, observedState) ->
            val stateT = sim.monteCarloIntegrate(sample, nSamples, obsInterval)
            val posterior = observedState.timesApproximate(stateT.asGroundedVector(sim.D0))
            sim.D0 = posterior.ground
            sample = posterior.basis
            println()
            println("real = $realState")
            println("observed = $observedState")
            println("posterior sample = $sample")
            println("posterior ground = ${posterior.ground}")
        }
    }
}