package experiments.spatialPredatorPrey.fock

import deselby.fockSpace.*
import deselby.fockSpace.extensions.asGroundedVector
import deselby.fockSpace.extensions.logProb
import deselby.std.Gnuplot
import deselby.std.gnuplot
import experiments.spatialPredatorPrey.Params
import experiments.spatialPredatorPrey.SmallParams
import experiments.spatialPredatorPrey.StandardParams
import org.junit.Test
import kotlin.math.exp

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
    fun assimilateFromInitialPoisson() {
        val sim = Simulation(SmallParams)
        val obsInterval = 0.1
        val pObserve = 0.25
        val nObservations = 10
        var sample= Basis.identity<Agent>()
        val observations = ObservationGenerator.generate(sim.params, pObserve, nObservations, obsInterval)

        println("observations = ${observations}")
        println("prior ground = ${sim.D0}")

        val nSamples = 500000
        observations.forEach { (realState, observedState) ->
            println()
            println("real = $realState")
            println("observed = $observedState")
            val stateT = sim.monteCarloIntegrateParallel(sample, nSamples, 8, obsInterval)
            println("priorT size = ${stateT.size}  sum = ${stateT.values.sum()}")
            val renormalisedState = stateT / stateT.values.sum()
            val posterior = observedState.timesApproximate(renormalisedState.asGroundedVector(sim.D0))
            sim.D0 = posterior.ground
            sample = posterior.basis
            println("posterior sample = $sample")
            println("posterior ground = ${posterior.ground}")
            println("logProb of real state = ${posterior.logProb(realState)}")
            plot(posterior, realState, sim.params.GRIDSIZE)
        }
    }


    @Test
    fun assimilateFromKnownInitialState() {
        val params = Params(
                20,
                0.01,
                0.02,
                0.03,
                0.06,
                1.0,
                0.07,
                0.05,
                0.5,
                1.0
        )
        val sim = Simulation(params)
        val obsInterval = 0.5
        val pObserve = 0.5
        val nObservations = 2
        val observations = ObservationGenerator.generate(sim.params, pObserve, nObservations, obsInterval)
        var sample= CreationBasis(observations[0].real)
        sim.setLambdas { 1e-6 }

        println("observations = ${observations}")
        println("prior ground = ${sim.D0}")
        println("initial state = ${observations[0].real}")

        val nSamples = 20000
        observations.drop(1).forEach { (realState, observedState) ->
            println()
            println("real = $realState")
            println("observed = $observedState")
            val stateT = sim.monteCarloIntegrateParallel(sample, nSamples, 8, obsInterval)
            println("priorT size = ${stateT.size}  sum = ${stateT.values.sum()}")
            val renormalisedState = stateT / stateT.values.sum()
            val posterior = observedState.timesApproximate(renormalisedState.asGroundedVector(sim.D0))
            sim.D0 = posterior.ground
            sample = posterior.basis
            println("posterior sample = $sample")
            println("posterior ground = ${posterior.ground.lambdas.entries.sortedByDescending { it.value }}")
            println("logProb of real state = ${posterior.logProb(realState)}")
            println("mean prob of present agent = ${exp(posterior.logProb(realState)/realState.size)}")
//            plot(posterior, realState, sim.params.GRIDSIZE)
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
        }.toList()
//        println("rabbitdata = ${rabbitData.toList()}")

        with(gp) {
            val rData = heredoc(rabbitData,6)
            val fData = heredoc(foxData,6)
            invoke("set linetype 1 lc 'red'")
            invoke("set linetype 2 lc 'blue'")
            invoke("plot $rData with rgbalpha")
            invoke("replot $fData with rgbalpha")
            if(realData.isNotEmpty()) {
                val pointData = heredoc(realData, 3)
                invoke("replot $pointData with points pointtype 5 pointsize 0.5 lc variable")
            }
            flush()
        }
    }

    fun plot(means: Map<Agent, Double>, gridSize: Int) {
        val plotData  = means.asSequence()
                .flatMap { (agent, lambda) ->
                    sequenceOf<Number>(agent.pos.rem(gridSize), agent.pos.div(gridSize), lambda*300, 0, 0)
                }

        with(gp) {
            val rData = heredoc(plotData,5)
            invoke("plot $rData with rgbimage")
            flush()
        }
    }


    fun prior(startState: CreationBasis<Agent>, params: Params, time: Double, nSamples: Int): Map<Agent, Double> {
        val sim = Simulation(params)
        val prior = sim.monteCarloIntegrateParallel(startState, nSamples, 8, time)
        println("prior size = ${prior.size} sum = ${prior.values.sum()}")
        return sim.D0.mean(prior)
    }


    fun posterior(startState: CreationBasis<Agent>, params: Params, observations: BinomialBasis<Agent>, time: Double, nSamples: Int): Map<Agent, Double> {
        val sim = Simulation(params)
        val prior = sim.monteCarloIntegrateParallel(startState, nSamples, 8, time)
        println("prior size = ${prior.size} sum = ${prior.values.sum()}")
        val posterior = observations.timesApproximate(prior.asGroundedVector(sim.D0))
        return posterior.ground.lambdas.mapValues { it.value + posterior.basis[it.key] }
    }

}