package experiments.SIR

import deselby.mcmc.MetropolisHastings
import deselby.mcmc.Observations
import deselby.mcmc.mean
import deselby.mcmc.standardDeviation
import deselby.std.nextExponential
import deselby.std.nextPoisson
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
    }

    data class DoubleSIRState(var S: Double, var I: Double, var R: Double) {
        fun step(p: SIRParams, dt : Double) : DoubleSIRState {
            val dS_dt = -p.beta * S * I
            val dR_dt = p.gamma * I
            val dI_dt = -(dS_dt + dR_dt)
            return DoubleSIRState(S + dS_dt*dt, I + dI_dt*dt, R + dR_dt*dt)
        }
    }

    data class SIRParams(val beta: Double, val gamma:Double, val rand: RandomGenerator) {
        fun simulateAndObserve(startState: SIRState, observationInterval: Double, totalTime: Double) : ArrayList<SIRState> {
            val m = ArrayList<SIRState>((totalTime/observationInterval + 1.0).toInt())
            var t = 0.0
            var p = startState
            m.add(startState)
            while(t < totalTime) {
                val (nextP, dt) = p.nextState(this, rand)
                p = nextP
                t += dt
                while(min(t, totalTime) > m.size*observationInterval) {
                    m.add(p)
                }
            }
            return m
        }

        fun simulateAndObserve(startState: DoubleSIRState, observationInterval: Double, totalTime: Double) : ArrayList<DoubleSIRState> {
            val m = ArrayList<DoubleSIRState>((totalTime/observationInterval + 1.0).toInt())
            var t = 0.0
            m.add(startState)
            var p = startState
            while(t < totalTime) {
                for(i in 1..10) p = p.step(this, observationInterval/10.0)
                m.add(p)
                t += observationInterval
            }
            return m
        }

        fun generateObservations(s0 : SIRState, observationInterval : Double, detectionProb : Double, totalTime : Double) : Array<Int> {
            val sim = simulateAndObserve(s0, observationInterval, totalTime)
            val observations = Array(sim.size) {i ->
                BinomialDistribution(sim[i].I, detectionProb).sample()
            }
            return observations
        }

    }


    fun metropolisHastingsPosterior(observations : Array<Int>, observationInterval: Double, r: Double) {
        val totalTime = observationInterval * observations.size
        val mcmc = MetropolisHastings { rand ->
            val params = SIRParams(0.01, 0.1, rand)
            val initState = SIRState(rand.nextPoisson(40.0), rand.nextPoisson(7.0), 0)
            val sim = params.simulateAndObserve(initState, observationInterval, totalTime)
            val observe = Observations()
            for (i in 0 until sim.size) {
                observe.binomial(r, sim[i].I, observations[i])
            }
            Pair(observe, sim.last().I)
        }
        mcmc.sampleWithGaussianProposal(100000, 0.1)
        println(observations.asList())
        println("Ibar = ${mcmc.mean()} sd = ${mcmc.standardDeviation()}")
    }
}