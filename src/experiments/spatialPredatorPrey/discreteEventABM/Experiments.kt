package experiments.spatialPredatorPrey.discreteEventABM

import experiments.spatialPredatorPrey.Params
import experiments.spatialPredatorPrey.StandardParams
import experiments.spatialPredatorPrey.TenByTenParams
import experiments.spatialPredatorPrey.TestParams
import org.apache.commons.math3.distribution.BinomialDistribution
import org.apache.commons.math3.special.Gamma.logGamma
import org.junit.Test
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow
import kotlin.system.measureTimeMillis

class Experiments {

    @Test
    fun simulate() {
        val plotter = StatePlotter()
        val sim = Simulation(TestParams)
        for(i in 1..1000) {
            sim.simulate(0.2)
//        println("${sim.predatorMultiplicity()} ${sim.preyMultiplicity()} ${sim.averageMultiplicity()}")
            plotter.gp("pause 0.01")
            plotter.plot(sim)
        }
    }


    fun prior(startState: Map<Agent,Int>, params: Params, time: Double, nSamples: Int) : HashMap<Int, Double> {
        val agentDensity = HashMap<Int, Double>()
        for(i in 1..nSamples) {
            val sim = Simulation(params)
            startState.forEach { (agent, count) -> for(i in 1..count) sim.add(agent.copy()) }
            sim.simulate(time)
            sim.agents.forEach { agentDensity.merge(it.hashCode(), 1.0, Double::plus) }
        }
        agentDensity.entries.forEach { it.setValue(it.value / nSamples) }
        return agentDensity
    }

    fun posterior(startState: Map<Agent,Int>, params: Params, observations: Map<Int,Int>, pObserve: Double, time: Double, nSamples: Int) : HashMap<Int, Double> {
        val agentDensity = HashMap<Int, Double>()
        var sumOfLikelihoods = 0.0
        for(i in 1..nSamples) {
            val sim = Simulation(params)
            startState.forEach { (agent, count) -> for(i in 1..count) sim.add(agent.copy()) }
            sim.simulate(time)
            var likelihood = 1.0
            for(pos in 0 until params.GRIDSIZESQ) {
                val pred = Predator(pos,params.GRIDSIZE)
                val prey = Prey(pos,params.GRIDSIZE)
//                println("prey = ${prey.hashCode()} ${sim.count(prey)} ${observations[prey.hashCode()]}")
//                println("pred = ${pred.hashCode()} ${sim.count(pred)} ${observations[pred.hashCode()]}")
                likelihood *= binomial(pObserve, sim.count(pred), observations[pred.hashCode()]?:0)
                likelihood *= binomial(pObserve, sim.count(prey), observations[prey.hashCode()]?:0)
            }
            sumOfLikelihoods += likelihood
            sim.agents.forEach { agentDensity.merge(it.hashCode(), likelihood, Double::plus) }
        }
        println("sum of likelihoods = $sumOfLikelihoods")
        agentDensity.entries.forEach { it.setValue(it.value / sumOfLikelihoods) }
        return agentDensity
    }

    @Test
    fun plotObservations() {
        val plotter = StatePlotter()
        val pObserve = 0.1
        val nObservations = 10
        val sim = Simulation(StandardParams)
        val observationList = sim.generateObservations(pObserve, nObservations, 1.0)
        for(observation in observationList) {
            val plotData = observation.observed.asSequence().flatMap { (agent,_) ->
                sequenceOf(agent.xPos, agent.yPos, if(agent is Prey) 1 else 2)
            }
            plotter.gp("plot [0:${sim.params.GRIDSIZE}][0:${sim.params.GRIDSIZE}] '-' binary record=(${observation.observed.size}) using 1:2:3 with points pointtype 5 pointsize 0.5 lc variable")
            plotter.gp.write(plotData)
            plotter.gp("pause 1.0")
        }
    }

    fun binomial(p : Double, n : Int, k : Int) : Double {
        if(k > n || k < 0) return 0.0
        var binom = p.pow(k)
        val notP = 1.0-p
        for(i in k+1..n) {
            binom *= i*notP/(i-k)
        }
        return binom
    }

}