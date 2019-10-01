package experiments.spatialPredatorPrey.fock

import deselby.fockSpace.*
import deselby.fockSpace.extensions.*
import deselby.std.Gnuplot
import experiments.reverseSummation.reverseIntegrateAndSum
import experiments.spatialPredatorPrey.*
import org.junit.Test
import kotlin.math.absoluteValue
import kotlin.math.exp
import kotlin.math.ln
import kotlin.random.Random
import kotlin.system.measureTimeMillis

class Experiments {

    val gp = Gnuplot()


    @Test
    fun reversePosterior() {
        val sim = Simulation(TenByTenParams)
        val priorLambda = sim.D0.lambdas
        val obsInterval = 0.5
        val nWindows = 10   // number of assimilation windows
        val pLook = 0.02     // probability of looking at a agent state
        val pObserve = 0.9  // probability of detecting an agent given that we're looking
        var state = Basis.identity<Agent>()
        val allObservations = ObservationGenerator.generate(sim.params, pObserve, nWindows, obsInterval)
        val observations = allObservations.map { observation ->
            Pair(observation.real, BinomialBasis(observation.observed.pObserve,
                    sim.D0.lambdas.keys
                            .filter { Random.nextDouble() < pLook }
                            .associateWith { observation.observed.observations[it]?:0 }
            ))
        }

        val time = measureTimeMillis {
            observations.forEach { (realState, binomObs) ->
                println()
                println("Starting window in state $state ${sim.D0}")
                println("observation is $binomObs")
                val (nextState, nextD0) = sim.reversePosterior(state, binomObs, obsInterval, 3)
                sim.D0 = nextD0
                state = nextState
                val binomLogProb = binomObs.logProb(realState, priorLambda)
                val posteriorLogProb = state.asGroundedBasis(sim.D0).logProb(realState)
                println("binomial logProb of real state = $binomLogProb")
                println("posterior logProb of real state = $posteriorLogProb")
                println("extra bits of information = ${(posteriorLogProb - binomLogProb)/ln(2.0)}")
            }
        }
        println("Finished in state $state ${sim.D0}")
        println("time = $time")
    }


    @Test
    fun reversePrior() {
        val time = 0.5
        val sim = Simulation(TestParams)
        val newLambda = HashMap<Agent,Double>()
        val startState = Basis.identity<Agent>().asGroundedBasis(sim.D0)
        sim.D0.lambdas.forEach {(d, lambdad) ->
            val dmean = ActionBasis(emptySet(), d).toVector().reverseIntegrateAndSum(sim.hcIndex, time, startState, 2e-4)
//            val dmean = ActionBasis(emptySet(), d).toVector().reverseIntegrateAndSum(sim.hcIndex, time, startState, 3)
            newLambda[d] = dmean
            println("$d -> $dmean")
        }

    }

    @Test
    fun reverseExponentialComposition() {
        val time = 0.1
        val sim = Simulation(SmallParams)
        val a = Basis.annihilate<Agent>(Predator(0))
        val b = Basis.annihilate<Agent>(Prey(1))
        val ab = Basis.newBasis(emptyMap(),mapOf(Prey(1) as Agent to 1, Predator(0) as Agent to 1))
        val Eab = sim.reverseExponential(ab, time,6)
        val EabD = Eab * sim.D0
        println("Sum of EabD = ${EabD.values.sum()}")
        val Ea = sim.reverseExponential(a, time,6)
        println("Ea size = ${Ea.size}")
        val Eb = sim.reverseExponential(b, time,6)
        println("Eb size = ${Eb.size}")
        val EaD = Ea * sim.D0
//        val EbEaD = Eb * EaD *sim.D0
//        println("EbEaD size = ${EbEaD.size}")
//        println("EbEaD sum = ${EbEaD.values.sum()}")
        val EbD = Eb * sim.D0
//        val EaEbD = Ea * EbD *sim.D0
//        println("EaEbD size = ${EaEbD.size}")
//        println("EaEbD sum = ${EaEbD.values.sum()}")
        val prodOfSums = EaD.values.sum()*EbD.values.sum()
//        println("product of sums = ${prodOfSums}")
//        val commutationD = EaEbD - EbEaD
//        println("commutation size = ${commutationD.size}")
//        val commutationSum = commutationD.values.sum()
//        println("commutation sum = ${commutationSum}")
//        println("commutation norm = ${commutationD.normL1()}")
//        println("composed sum = ${prodOfSums + commutationSum}")

        val EaStripped = Ea.stripCreations()
        val semicommutation = EaStripped.semicommute(EbD.toCreationIndex()) * sim.D0
        println("semiommutation size = ${semicommutation.size}")
        println("semi-composed sum = ${prodOfSums + semicommutation.values.sum()}")
    }


    @Test
    fun reverseSumIntegrationWithStripping() {
        val time = 0.5
        val sim = Simulation(StandardParams)
//        val a = Basis.annihilate<Agent>(Predator(0))
//        val a = Basis.annihilate<Agent>(Prey(1))
        val a = Basis.newBasis(emptyMap(),mapOf(Prey(1) as Agent to 1, Predator(0) as Agent to 1))

        val marginalIntegral = sim.reverseExponential(a, time, 8,true)
        println(marginalIntegral.size)
    }


    @Test
    fun reverseMarginalisedIntegration() {
        val time = 0.5
        val sim = Simulation(StandardParams)
//        val a = Basis.annihilate<Agent>(Predator(0))
//        val b = Basis.annihilate<Agent>(Prey(1))
//        val ab = Basis.newBasis(emptyMap(),mapOf(Prey(1) as Agent to 1, Predator(0) as Agent to 1))

//        val marginalIntegral = sim.reverseMarginalisedIntegrate(listOf(Predator(0), Prey(1), Predator(2)), time)
        val marginalIntegral = sim.reverseMarginalisedIntegrate(listOf(Predator(0)), time)
        println(marginalIntegral.size)
    }


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
        println("monteCarloPrior ground = ${sim.D0}")

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
                8,
                0.1,
                0.2,
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
        val nObservations = 5
        val observations = ObservationGenerator.generate(sim.params, pObserve, nObservations, obsInterval)
        var sample= CreationBasis(observations[0].real)
        sim.setLambdas { 1e-6 }

        println("observations = ${observations}")
        println("monteCarloPrior ground = ${sim.D0}")
        println("initial state = ${observations[0].real}")

        val nSamples = 20000
        observations.drop(1).forEach { (realState, observedState) ->
            println()
            println("real = $realState")
            println("observed = $observedState")
            println("binomial logProb of real state = ${observedState.logLikelihood(CreationBasis(realState), sim.D0.lambdas.keys)}")
            val stateT = sim.monteCarloIntegrateParallel(sample, nSamples, 8, obsInterval)
            println("priorT size = ${stateT.size}  sum = ${stateT.values.sum()}")
            val renormalisedState = stateT / stateT.values.sum()
            val posterior = observedState.timesApproximate(renormalisedState.asGroundedVector(sim.D0))
            println("posterior logProb of real state = ${ln(observedState.posteriorProbability(renormalisedState.asGroundedVector(sim.D0), CreationBasis(realState)))}")
            sim.D0 = posterior.ground
            sample = posterior.basis
            println("posterior sample = $sample")
            println("posterior ground = ${posterior.ground.lambdas.entries.sortedByDescending { it.value }}")
            println("basis logProb of real state = ${posterior.logProb(realState)}")
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


    fun monteCarloPrior(startState: CreationBasis<Agent>, params: Params, time: Double, nSamples: Int): Map<Agent, Double> {
        val sim = Simulation(params)
        val prior = sim.monteCarloIntegrateParallel(startState, nSamples, 8, time)
        println("monteCarloPrior size = ${prior.size} sum = ${prior.values.sum()}")
        return prior.asGroundedVector(sim.D0).means()
    }


    fun reverseIntegralPrior(startState: CreationBasis<Agent>, params: Params, time: Double): Map<Agent, Double> {
        val sim = Simulation(params)
        val prior = sim.reverseIntegrateToBasis(startState, time)
        return prior.lambdas
    }


    fun posterior(startState: CreationBasis<Agent>, params: Params, observations: BinomialBasis<Agent>, time: Double, nSamples: Int): Map<Agent, Double> {
        val sim = Simulation(params)
        val prior = sim.monteCarloIntegrateParallel(startState, nSamples, 8, time)
        println("monteCarloPrior size = ${prior.size} sum = ${prior.values.sum()}")
        val posterior = observations.timesApproximate(prior.asGroundedVector(sim.D0))
        return posterior.ground.lambdas.mapValues { it.value + posterior.basis[it.key] }
    }



}