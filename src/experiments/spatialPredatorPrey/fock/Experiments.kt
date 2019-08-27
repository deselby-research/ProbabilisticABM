package experiments.spatialPredatorPrey.fock

import deselby.fockSpace.Basis
import deselby.fockSpace.CreationBasis
import deselby.fockSpace.GroundedBasis
import deselby.fockSpace.extensions.asGroundedVector
import deselby.fockSpace.extensions.logProb
import experiments.spatialPredatorPrey.SmallParams
import experiments.spatialPredatorPrey.StandardParams
import org.junit.Test

class Experiments {

    @Test
    fun simulate() {
        val integrationTime = 0.25
        val sim = Simulation(StandardParams)
        val startState = CreationBasis<Agent>(emptyMap()) //Basis.identity<Agent>()
//        val sample= GroundedBasis(startState, sim.D0)

//    val p = sample.integrate(sim.H, integrationTime, 0.001, 1e-8)
//    println("integral = $p")
        println()

        val nSamples = 10000
        val finalState = sim.monteCarloIntegrate(startState, nSamples, integrationTime)
        println("final state = $finalState")
        println("nTerms = ${finalState.size}")
        println("normalisation = ${finalState.values.sum()}")
    }

    @Test
    fun assimilate() {
        val sim = Simulation(SmallParams)
        val obsInterval = 1.0
        val pObserve = 0.5
        val nObservations = 10
        var sample= Basis.identity<Agent>()
        val observations = ObservationGenerator.generate(sim.params, pObserve, nObservations, obsInterval)

        println("prior ground = ${sim.D0}")

        val nSamples = 100000
        observations.forEach { (realState, observedState) ->
            println()
            println("real = $realState")
            println("observed = $observedState")
            val stateT = sim.monteCarloIntegrate(sample, nSamples, obsInterval)
            println("priorT size = ${stateT.size}  sum = ${stateT.values.sum()}")
            val posterior = observedState.timesApproximate(stateT.asGroundedVector(sim.D0))
            sim.D0 = posterior.ground
            sample = posterior.basis
            println("posterior sample = $sample")
            println("posterior ground = ${posterior.ground}")
            println("logProb of real state = ${posterior.logProb(realState)}")
        }
    }
}