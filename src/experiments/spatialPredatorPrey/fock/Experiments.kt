package experiments.spatialPredatorPrey.fock

import deselby.fockSpace.Basis
import deselby.fockSpace.CreationBasis
import deselby.fockSpace.DeselbyGround
import deselby.fockSpace.GroundedBasis
import deselby.fockSpace.extensions.asGroundedVector
import deselby.fockSpace.extensions.logProb
import deselby.std.Gnuplot
import deselby.std.gnuplot
import experiments.spatialPredatorPrey.SmallParams
import experiments.spatialPredatorPrey.StandardParams
import org.junit.Test

class Experiments {

    val gp = Gnuplot()

    @Test
    fun simulate() {
        val integrationTime = 0.25
        val sim = Simulation(StandardParams)
        val startState = CreationBasis<Agent>(emptyMap()) //Basis.identity<Agent>()
//        val sample= GroundedBasis(startState, sim.D0)

//    val p = sample.integrate(sim.H, integrationTime, 0.001, 1e-8)
//    println("integral = $p")
        println()

        val nSamples = 100000
        val finalState = sim.monteCarloIntegrate(startState, nSamples, integrationTime)
        println("final state = $finalState")
        println("nTerms = ${finalState.size}")
        println("normalisation = ${finalState.values.sum()}")
    }

    @Test
    fun assimilate() {
        val sim = Simulation(SmallParams)
        val obsInterval = 0.1
        val pObserve = 0.25
        val nObservations = 10
        var sample= Basis.identity<Agent>()
        val observations = ObservationGenerator.generate(sim.params, pObserve, nObservations, obsInterval)

        println("observations = ${observations}")
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
            plot(posterior, realState, sim.params.GRIDSIZE)
        }
    }

    fun plot(state: GroundedBasis<Agent, DeselbyGround<Agent>>, realState: Map<Agent,Int>, gridSize: Int) {
        val rabbitData  = state.ground.lambdas.asSequence()
                .filter { it.key is Prey }
                .flatMap { (agent, lambda) ->
                    sequenceOf<Number>(agent.pos.rem(gridSize), agent.pos.div(gridSize), (state.basis[agent] + lambda)*300, 0, 0, 128)
                }

        val foxData = state.ground.lambdas.asSequence()
                .filter { it.key is Predator }
                .flatMap { (agent, lambda) ->
                    sequenceOf<Number>(agent.pos.rem(gridSize), agent.pos.div(gridSize), 0, 0, (state.basis[agent] + lambda)*300, 128)
                }

        val realData = realState.entries.asSequence().flatMap {(agent, _) ->
            sequenceOf(agent.pos.rem(gridSize), agent.pos.div(gridSize), if(agent is Prey) 1 else 2)
        }
//        println("rabbitdata = ${rabbitData.toList()}")

        with(gp) {
            val rData = heredoc(rabbitData,6)
            val fData = heredoc(foxData,6)
            val pointData = heredoc(realData, 3)
            invoke("set linetype 1 lc 'red'")
            invoke("set linetype 2 lc 'blue'")
            invoke("plot $rData with rgbalpha")
            invoke("replot $fData with rgbalpha")
            invoke("replot $pointData with points pointtype 5 pointsize 0.5 lc variable")
            flush()
        }
    }

}