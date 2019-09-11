package experiments.SIR

import deselby.mcmc.MetropolisHastings
import deselby.mcmc.Observations
import deselby.std.extensions.statistics
import deselby.std.extensions.nextExponential
import deselby.std.extensions.nextPoisson
import org.apache.commons.math3.distribution.BinomialDistribution
import org.apache.commons.math3.random.RandomGenerator
import kotlin.math.min

// Simulates
// beta = rate of infection per infected suseptible
// gamma = rate of recovery per infected
object NonFockSIR {

    data class SIRState(val S: Int, val I: Int, val R: Int) {
        fun nextState(p: SIRParams, rand: RandomGenerator) : Pair<SIRState, Double> {
            val totalRate = (p.beta * S + p.gamma) * I
            val recoveryRate = p.gamma * I
            val dt = rand.nextExponential(totalRate)
            val nextState = if(totalRate * rand.nextDouble() < recoveryRate)
                SIRState(S,I-1, R+1) else SIRState(S-1,I+1,R)
            return Pair(nextState, dt)
        }

        operator fun plus(other: SIRState) : SIRState {
            return SIRState(S+other.S, I+other.I, R+other.R)
        }
    }

    data class DoubleSIRState(var S: Double, var I: Double, var R: Double) {
        fun step(p: SIRParams, dt : Double) : DoubleSIRState {
            val dS_dt = -p.beta * S * I
            val dR_dt = p.gamma * I
            val dI_dt = -(dS_dt + dR_dt)
            return DoubleSIRState(S + dS_dt*dt, I + dI_dt*dt, R + dR_dt*dt)
        }
    }

    class SIRSimulator(val params: SIRParams, val rand: RandomGenerator) {
        fun simulateAndObserve(startState: SIRState, observationInterval: Double, totalTime: Double): ArrayList<SIRState> {
            val m = ArrayList<SIRState>((totalTime / observationInterval + 1.0).toInt())
            var t = 0.0
            var p = startState
            m.add(startState)
            while (t < totalTime) {
                val (nextP, dt) = p.nextState(params, rand)
                t += dt
                while (min(t, totalTime) >= m.size * observationInterval) {
                    m.add(p)
                }
                p = nextP
            }
            return m
        }

        fun simulateAndObserve(startState: DoubleSIRState, observationInterval: Double, totalTime: Double): ArrayList<DoubleSIRState> {
            val m = ArrayList<DoubleSIRState>((totalTime / observationInterval + 1.0).toInt())
            var t = 0.0
            m.add(startState)
            var p = startState
            while (t < totalTime) {
                for (i in 1..10) p = p.step(params, observationInterval / 10.0)
                m.add(p)
                t += observationInterval
            }
            return m
        }

        fun generateObservations(s0: SIRState, observationInterval: Double, detectionProb: Double, totalTime: Double): Array<Int> {
            val sim = simulateAndObserve(s0, observationInterval, totalTime)
            val observations = Array(sim.size) { i ->
                BinomialDistribution(sim[i].I, detectionProb).sample()
            }
            return observations
        }

        fun prior(nSamples: Int, T: Double) {
            val simulator = SIRSimulator(params, rand)
            val samples = ArrayList<SIRState>(nSamples)
            for(i in 1..nSamples) {
                val initState = SIRState(rand.nextPoisson(params.lambdaS), rand.nextPoisson(params.lambdaI), 0)
                val finalState = simulator.simulateAndObserve(initState, T, T).last()
                samples.add(finalState)
            }
            val Sstats = samples.asSequence().map{it.S}.statistics()
            val Istats = samples.asSequence().map{it.I}.statistics()

            println("S statistics = ${Sstats}")
            println("I statistics = ${Istats}")
        }

    }


    fun MCMCPosterior(observations: Array<Int>, observationInterval: Double, params: SIRParams, r: Double, nSamples: Int) {
        val totalTime = observationInterval * (observations.size-0.5)
        val mcmc = MetropolisHastings { rand ->
            val simulator = SIRSimulator(params, rand)
            val initState = SIRState(rand.nextPoisson(params.lambdaS), rand.nextPoisson(params.lambdaI), 0)
            val sim = simulator.simulateAndObserve(initState, observationInterval, totalTime)
            val observe = Observations()
            for (i in 0 until sim.size) {
                observe.binomial(r, sim[i].I, observations[i])
            }
            Pair(observe.logp, sim.last())
        }
        val samples = mcmc.sampleToList(nSamples)
        println(observations.asList())
        println("Sstats = ${samples.asSequence().map {it.S}.drop(10000).statistics()}")
        println("Istats = ${samples.asSequence().map {it.I}.drop(10000).statistics()}")
    }

}